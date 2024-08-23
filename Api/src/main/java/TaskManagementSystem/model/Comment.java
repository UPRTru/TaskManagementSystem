package TaskManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import java.util.Date;

@Entity
@Data
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "text")
  private String text;

  @ManyToOne
  @JoinColumn(name = "author")
  private User author;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date", columnDefinition = "timestamp default now()")
  private Date date = new Date();

  @ManyToOne
  @JoinColumn(name = "task")
  private Task task;
}
