package TaskManagementSystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collection;
import java.util.Date;

@Entity
@Data
@Table(name = "tasks")
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", unique = true)
  private String name;

  @Column(name = "description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority")
  private Priority priority;

  @ManyToOne
  @JoinColumn(name = "author")
  private User author;

  @ManyToOne
  @JoinColumn(name = "executor")
  private User executor;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date", columnDefinition = "timestamp default now()")
  private Date date = new Date();

  @OneToMany(mappedBy = "task")
  private Collection<Comment> comments;
}
