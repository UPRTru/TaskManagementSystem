# TaskManagementSystem
Менеджер задач

В сервисе прописана авторизация (Spring Security). Во время авторизации выдается JWT торен. Программа принимает токен из запроса, проверят пользователя и авторизацию, и выполняет запрос далее при успехе.

Сервис позволяет создовать задачи.
Каждый пользователь может создать задачу.
Автор задачи может ее редактировать, удалять, менять приоритет.
Исполнителем задачи может стать любой пользователь самостоятельно (если исполнитель еще не назначен). Либо его может установить автор задачи или убрать исполнителя щадачи совсем.
При создании новой задачи ее статус - "В ожидании".
Когда у задачи появляется исполнитель: статус меняется на - "в процессе".
Если задача осталось без исполнителя: статус снова "В ожидании".
Статус задачи "Выполнена" может поставить автор или исполнитель.

Каждый пользователь может просматривать задачи других пользователей/ задачи в которых он исполнитель/ все задачи.
Полученные задачи выводятся в виде "Page". В запросе можно указывать количество страниц и объектов на странице, и ставить фильтр сортировки по дате.

Каждый пользователь может оставлять комментарии к задачам.
Автор может изменять комментарий.
По запросу можно получить список комментариев к задаче в виде "Page" и указать сортировку по дате, количество страниц/объектов на странице.

Классы "Entity" и программа настроены так, что бы при подключении к базе данных в нее загружались новые таблицы под эти классы/ обновление таблиц под классы.

Написаны тесты под основные функции программы.
Каждяя ошибка выводит нужный HTML код ошибки с ее текстом.


