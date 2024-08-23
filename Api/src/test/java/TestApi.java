import TaskManagementSystem.Api;
import TaskManagementSystem.dto.ResultCommentDto;
import TaskManagementSystem.dto.ResultTaskDto;
import TaskManagementSystem.dto.TokenDto;
import TaskManagementSystem.model.Priority;
import TaskManagementSystem.model.Status;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = Api.class)
public class TestApi {
    Gson gson = new Gson();

    private enum Method {
        GET,
        POST,
        PATCH,
        DELETE
    }

    @Test
    public void test() throws IOException {
        String jsonUser = "{\"email\":\"user@email.com\",\"password\":\"password\"}";
        String jsonUser2 = "{\"email\":\"user2@email.com\",\"password\":\"password\"}";
        String jsonUser3 = "{\"email\":\"user3@email.com\",\"password\":\"password\"}";
        String jsonUserErrorEmail = "{\"email\":\"mail\",\"password\":\"password\"}";
        String jsonTask = "{\"name\":\"task\",\"description\":\"description\"}";
        String jsonTask1_2 = "{\"name\":\"task1_2\",\"description\":\"description\"}";
        String jsonTask2 = "{\"name\":\"task2\",\"description\":\"description\"}";
        String jsonTask3 = "{\"name\":\"task3\",\"description\":\"description\"}";
        String jsonTaskUpdate = "{\"name\":\"\",\"description\":\"test description\"}";
        String jsonComment = "{\"text\":\"text\"}";
        String jsonCommentUpdate = "{\"text\":\"test text\"}";

        // тест проверки емейла
        Map<String, String> result = httpResult("http://localhost:8080/registration", jsonUserErrorEmail, Method.POST, null);
        assertEquals("HTTP/1.1 400 ", result.get("status"));
        assertEquals("Ошибка: Некорректная почта mail", result.get("body"));

        //тест регистрации
        result = httpResult("http://localhost:8080/registration", jsonUser, Method.POST, null);
        assertEquals("HTTP/1.1 201 ", result.get("status"));
        assertEquals("Пользователь user@email.com создан", result.get("body"));

        //тест повторной регистрации с тем же емейлом
        result = httpResult("http://localhost:8080/registration", jsonUser, Method.POST, null);
        assertEquals("HTTP/1.1 208 ", result.get("status"));
        assertEquals("Ошибка: Пользователь user@email.com уже существует", result.get("body"));

        //тест получения токена
        result = httpResult("http://localhost:8080/auth", jsonUser, Method.POST, null);
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        String token = result.get("body");
        assertTrue(token.startsWith("{\"token"));
        TokenDto tokenDto = gson.fromJson(token, TokenDto.class);

        //тест проверка что задач нет
        result = httpResult("http://localhost:8080/getMyCreateTasks/0,10/NONE", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 404 ", result.get("status"));
        assertEquals("Ошибка: У пользователя user@email.com нет задач", result.get("body"));

        //тест создания задачи с некорректным токеном
        result = httpResult("http://localhost:8080/createTask", jsonTask, Method.POST, "ERROR_TOKEN");
        assertEquals("HTTP/1.1 401 ", result.get("status"));

        //тест создания задачи
        result = httpResult("http://localhost:8080/createTask", jsonTask, Method.POST, tokenDto.getToken());
        assertEquals("HTTP/1.1 201 ", result.get("status"));
        assertEquals("Задача task создана", result.get("body"));

        //тест получения задачи
        result = httpResult("http://localhost:8080/getTaskByName/task", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        assertEquals("{\"name\":\"task\"," +
                "\"description\":\"description\"," +
                "\"status\":\"PENDING\"," +
                "\"priority\":\"MEDIUM\"," +
                "\"author\":{\"email\":\"user@email.com\",\"password\":null}," +
                "\"executor\":null," +
                "\"date\":null}", result.get("body"));

        //тест получения задач конкретного автора
        result = httpResult("http://localhost:8080/getAuthorTasks/user@email.com/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        PageTest pageTest = getPageTest(result.get("body"));
        assertEquals(1, pageTest.getTotalElements());
        assertEquals(1, pageTest.getContent().size());
        assertEquals("task", pageTest.getContent().get(0).getName());

        //тест пользователь не найден
        result = httpResult("http://localhost:8080/getAuthorTasks/error@email.com/0,10/NONE", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Пользователь error@email.com не найден", result.get("body"));

        //тест изменение задачи
        result = httpResult("http://localhost:8080/updateTask/task", jsonTaskUpdate, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        ResultTaskDto resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getDescription(), "test description");

        //тест повышения приоритета задачи
        result = httpResult("http://localhost:8080/updatePriorityUp/task", null, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getPriority(), Priority.HIGH);

        //тест понижение приоритета задачи
        httpResult("http://localhost:8080/updatePriorityDown/task", jsonTaskUpdate, Method.PATCH, tokenDto.getToken());
        result = httpResult("http://localhost:8080/updatePriorityDown/task", jsonTaskUpdate, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getPriority(), Priority.LOW);

        //тест создания комментария
        result = httpResult("http://localhost:8080/createComment/task", jsonComment, Method.POST, tokenDto.getToken());
        assertEquals("HTTP/1.1 201 ", result.get("status"));
        assertEquals("Комментарий добавлен", result.get("body"));

        //тест получения задач авторизованного пользователя (порядок по увеличению даты)
        httpResult("http://localhost:8080/createTask", jsonTask1_2, Method.POST, tokenDto.getToken());
        result = httpResult("http://localhost:8080/getMyCreateTasks/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest = getPageTest(result.get("body"));
        assertEquals(2, pageTest.getTotalElements());
        assertEquals("task", pageTest.getContent().get(0).getName());
        assertEquals("task1_2", pageTest.getContent().get(1).getName());
        //тест получения задач авторизованного пользователя (порядок по уменьшению даты)
        result = httpResult("http://localhost:8080/getMyCreateTasks/0,10/DOWN", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest = getPageTest(result.get("body"));
        assertEquals(2, pageTest.getTotalElements());
        assertEquals("task1_2", pageTest.getContent().get(0).getName());
        assertEquals("task", pageTest.getContent().get(1).getName());

        //__________________________________________________________________________________________________
        //подготовка к следующим тестам
        httpResult("http://localhost:8080/registration", jsonUser2, Method.POST, null);
        httpResult("http://localhost:8080/registration", jsonUser3, Method.POST, null);
        TokenDto tokenDto2 = gson.fromJson(httpResult("http://localhost:8080/auth", jsonUser2, Method.POST, null).get("body"), TokenDto.class);
        TokenDto tokenDto3 = gson.fromJson(httpResult("http://localhost:8080/auth", jsonUser3, Method.POST, null).get("body"), TokenDto.class);
        httpResult("http://localhost:8080/createTask", jsonTask2, Method.POST, tokenDto2.getToken());
        httpResult("http://localhost:8080/createTask", jsonTask3, Method.POST, tokenDto3.getToken());
        httpResult("http://localhost:8080/createComment/task", jsonComment, Method.POST, tokenDto2.getToken());
        httpResult("http://localhost:8080/createComment/task2", jsonComment, Method.POST, tokenDto.getToken());
        //__________________________________________________________________________________________________

        //тест пользователю не пренадлежит
        result = httpResult("http://localhost:8080/updateTask/task", jsonTaskUpdate, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task не принадлежит user2@email.com", result.get("body"));
        result = httpResult("http://localhost:8080/updatePriorityUp/task", null, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task не принадлежит user2@email.com", result.get("body"));
        result = httpResult("http://localhost:8080/updatePriorityDown/task", null, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task не принадлежит user2@email.com", result.get("body"));
        result = httpResult("http://localhost:8080/updateStatusComplete/task3", null, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task3 не принадлежит user2@email.com", result.get("body"));
        result = httpResult("http://localhost:8080/setExecutor/task/user2@email.com", null, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task не принадлежит user2@email.com", result.get("body"));
        result = httpResult("http://localhost:8080/setExecutor/task/null", null, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task не принадлежит user2@email.com", result.get("body"));
        result = httpResult("http://localhost:8080/deleteTask/task", null, Method.DELETE, tokenDto2.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Задача task не принадлежит user2@email.com", result.get("body"));

        //тест количество комментариев
        result = httpResult("http://localhost:8080/getComments/task/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        PageTest2 pageTest2 = getPageTest2(result.get("body"));
        assertEquals(2, pageTest2.getTotalElements());
        assertEquals(2, pageTest2.getContent().size());

        //тест изменения исполнителя
        result = httpResult("http://localhost:8080/setExecutor/task/user2@email.com", jsonUser, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getExecutor().getEmail(), "user2@email.com");
        assertEquals(resultTaskDto.getStatus(), Status.IN_PROGRESS);

        //тест пользователь не может взять задачу с исполнителем
        result = httpResult("http://localhost:8080/I_will_work_on_this_task/task", null, Method.PATCH, tokenDto3.getToken());
        assertEquals("HTTP/1.1 208 ", result.get("status"));
        assertEquals("Ошибка: У задачи task уже есть исполнитель", result.get("body"));

        //тест авторизированный пользователь становится исполнителем
        result = httpResult("http://localhost:8080/I_will_work_on_this_task/task2", jsonUser, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getExecutor().getEmail(), "user@email.com");
        assertEquals(resultTaskDto.getStatus(), Status.IN_PROGRESS);

        //тест получения задач исполнения
        result = httpResult("http://localhost:8080/getMyWorkTasks/0,10/UP", jsonUser, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest = getPageTest(result.get("body"));
        assertEquals(1, pageTest.getTotalElements());
        assertEquals(1, pageTest.getContent().size());
        assertEquals("task2", pageTest.getContent().get(0).getName());

        //тест завершения задачи автором
        result = httpResult("http://localhost:8080/updateStatusComplete/task", null, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getStatus(), Status.COMPLETED);

        //тест завершения задачи исполнителем
        result = httpResult("http://localhost:8080/updateStatusComplete/task2", null, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getStatus(), Status.COMPLETED);

        //тест изменения комментария
        result = httpResult("http://localhost:8080/updateComment/task", jsonCommentUpdate, Method.PATCH, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        ResultCommentDto resultCommentDto = gson.fromJson(result.get("body"), ResultCommentDto.class);
        assertEquals("test text", resultCommentDto.getText());

        //тест комментарий не найден
        result = httpResult("http://localhost:8080/updateComment/task", jsonCommentUpdate, Method.PATCH, tokenDto3.getToken());
        assertEquals("HTTP/1.1 404 ", result.get("status"));
        assertEquals("Ошибка: Комментарий user3@email.com не найден", result.get("body"));
        result = httpResult("http://localhost:8080/deleteComment/task", jsonCommentUpdate, Method.DELETE, tokenDto3.getToken());
        assertEquals("HTTP/1.1 404 ", result.get("status"));
        assertEquals("Ошибка: Комментарий user3@email.com не найден", result.get("body"));

        //тест удаления комментария
        result = httpResult("http://localhost:8080/getComments/task/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest2 = getPageTest2(result.get("body"));
        assertEquals(2, pageTest2.getTotalElements());
        assertEquals(2, pageTest2.getContent().size());
        result = httpResult("http://localhost:8080/deleteComment/task", null, Method.DELETE, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        assertEquals("Комментарий удален", result.get("body"));
        result = httpResult("http://localhost:8080/getComments/task/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest2 = getPageTest2(result.get("body"));
        assertEquals(1, pageTest2.getTotalElements());
        assertEquals(1, pageTest2.getContent().size());

        //тест получение всех задач (сортировка по дате UP)
        result = httpResult("http://localhost:8080/getAllTasks/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest = getPageTest(result.get("body"));
        assertEquals(4, pageTest.getTotalElements());
        assertEquals(4, pageTest.getContent().size());
        assertEquals("task", pageTest.getContent().get(0).getName());
        assertEquals("task3", pageTest.getContent().get(3).getName());

        //тест получения всех задач (сортировка по дате DOWN)
        result = httpResult("http://localhost:8080/getAllTasks/0,10/DOWN", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest = getPageTest(result.get("body"));
        assertEquals(4, pageTest.getTotalElements());
        assertEquals(4, pageTest.getContent().size());
        assertEquals("task3", pageTest.getContent().get(0).getName());
        assertEquals("task", pageTest.getContent().get(3).getName());

        //удаление пользователя
        //проверка комментария перед удалением автора и что он является исполнителем
        result = httpResult("http://localhost:8080/getTaskByName/task2", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getStatus(), Status.COMPLETED);
        assertEquals(resultTaskDto.getExecutor().getEmail(), "user@email.com");
        result = httpResult("http://localhost:8080/getComments/task2/0,10/UP", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest2 = getPageTest2(result.get("body"));
        assertEquals(1, pageTest2.getTotalElements());
        assertEquals(1, pageTest2.getContent().size());
        //тест удаления авторизированного пользователя
        result = httpResult("http://localhost:8080/deleteUser", null, Method.DELETE, tokenDto.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        assertEquals("Пользователь user@email.com удален", result.get("body"));
        //проверка удаления задач и комментариев пользователя после его удаления
        result = httpResult("http://localhost:8080/getTaskByName/task", null, Method.GET, tokenDto2.getToken());
        assertEquals("HTTP/1.1 404 ", result.get("status"));
        assertEquals("Ошибка: Задача task не найдена", result.get("body"));
        result = httpResult("http://localhost:8080/getTaskByName/task2", null, Method.GET, tokenDto2.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getStatus(), Status.PENDING);
        assertNull(resultTaskDto.getExecutor());
        result = httpResult("http://localhost:8080/getComments/task2/0,10/UP", null, Method.GET, tokenDto2.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        pageTest2 = getPageTest2(result.get("body"));
        assertEquals(0, pageTest2.getTotalElements());
        assertEquals(0, pageTest2.getContent().size());
        //проверка что пользователя нет
        result = httpResult("http://localhost:8080/getAuthorTasks/user@email.com/0,10/NONE", null, Method.GET, tokenDto.getToken());
        assertEquals("HTTP/1.1 401 ", result.get("status"));
        assertEquals("Ошибка: Пользователь user@email.com не найден", result.get("body"));

        //тест удаления исполнителя из задачи
        result = httpResult("http://localhost:8080/I_will_work_on_this_task/task2", jsonUser, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertEquals(resultTaskDto.getExecutor().getEmail(), "user2@email.com");
        assertEquals(resultTaskDto.getStatus(), Status.IN_PROGRESS);
        result = httpResult("http://localhost:8080/setExecutor/task2/null", null, Method.PATCH, tokenDto2.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        resultTaskDto = gson.fromJson(result.get("body"), ResultTaskDto.class);
        assertNull(resultTaskDto.getExecutor());
        assertEquals(resultTaskDto.getStatus(), Status.PENDING);

        //тест удаление задачи
        result = httpResult("http://localhost:8080/deleteTask/task2", null, Method.DELETE, tokenDto2.getToken());
        assertEquals("HTTP/1.1 200 ", result.get("status"));
        assertEquals("Задача task2 удалена", result.get("body"));
        result = httpResult("http://localhost:8080/getTaskByName/task2", null, Method.GET, tokenDto2.getToken());
        assertEquals("HTTP/1.1 404 ", result.get("status"));
        assertEquals("Ошибка: Задача task2 не найдена", result.get("body"));
    }

    //задачи
    private PageTest getPageTest(String json) {
        return gson.fromJson(json, PageTest.class);
    }

    //комментарии
    private PageTest2 getPageTest2(String json) {
        return gson.fromJson(json, PageTest2.class);
    }

    private Map<String, String> httpResult(String url, String json, Method method, String token) throws IOException {
        Map<String, String> result = new HashMap<>();
        if (method == Method.GET) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");
            if (token != null) {
                httpGet.setHeader("Authorization", "Bearer " + token);
            }
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
            String status = httpResponse.getStatusLine().toString();
            result.put("status", status);
            String body = EntityUtils.toString(httpResponse.getEntity());
            result.put("body", body);
            return result;
        } else if (method == Method.POST) {
            HttpPost httpPost = new HttpPost(url);
            if (json != null) {
                httpPost.setEntity(new StringEntity(json));
            }
            httpPost.setHeader("Content-Type", "application/json");
            if (token != null) {
                httpPost.setHeader("Authorization", "Bearer " + token);
            }
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
            String status = httpResponse.getStatusLine().toString();
            result.put("status", status);
            String body = EntityUtils.toString(httpResponse.getEntity());
            result.put("body", body);
            return result;
        } else if (method == Method.PATCH) {
            HttpPatch httpPatch = new HttpPatch(url);
            if (json != null) {
                httpPatch.setEntity(new StringEntity(json));
            }
            httpPatch.setHeader("Content-Type", "application/json");
            if (token != null) {
                httpPatch.setHeader("Authorization", "Bearer " + token);
            }
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPatch);
            String status = httpResponse.getStatusLine().toString();
            result.put("status", status);
            String body = EntityUtils.toString(httpResponse.getEntity());
            result.put("body", body);
            return result;
        } else if (method == Method.DELETE) {
            HttpDelete httpDelete = new HttpDelete(url);
            httpDelete.setHeader("Content-Type", "application/json");
            if (token != null) {
                httpDelete.setHeader("Authorization", "Bearer " + token);
            }
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpDelete);
            String status = httpResponse.getStatusLine().toString();
            result.put("status", status);
            String body = EntityUtils.toString(httpResponse.getEntity());
            result.put("body", body);
            return result;
        } else {
            return result;
        }
    }
}
