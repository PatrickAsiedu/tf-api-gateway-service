package app.tradeflows.api.api_gateway.entities;

import app.tradeflows.api.api_gateway.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Table(name = "tf_users")
@Entity
@Setter
@Getter
public class User extends Audit implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 200)
    private String id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    private String dob;
    private boolean isActive;

    public User(){}

    public User(String name, String email, String password, UserRole role, String dob, boolean isActive) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.dob = dob;
        this.isActive = isActive;
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setIsActive() {
         isActive = !isActive;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
