package shared.model;
public class User {
    private final long id;
    private final String name, username, role;
    public User(long id, String name, String username, String role) { this.id=id; this.name=name; this.username=username; this.role=role; }
    public long getId(){return id;} public String getName(){return name;} public String getUsername(){return username;} public String getRole(){return role;}
    public boolean isAdmin(){return "ADMIN".equals(role);}
}
