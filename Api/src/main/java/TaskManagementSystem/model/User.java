package TaskManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "locked")
  private int locked = 3; //количество неудачных попыток входа

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "password")
  private String password;
}

