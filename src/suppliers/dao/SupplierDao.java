package suppliers.dao;
import shared.config.Database;
import java.sql.*;
import java.util.*;
public class SupplierDao {
 public List<String[]> list()throws SQLException{List<String[]> a=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,phone,email,address,active FROM suppliers ORDER BY name");ResultSet r=p.executeQuery()){while(r.next())a.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBoolean(6)?"Active":"Disabled"});}return a;}
 public void add(String name,String phone,String email,String address)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO suppliers(name,phone,email,address) VALUES(?,?,?,?)")){p.setString(1,name);p.setString(2,phone);p.setString(3,email);p.setString(4,address);p.executeUpdate();}}
 public void setActive(long id,boolean active)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("UPDATE suppliers SET active=? WHERE id=?")){p.setBoolean(1,active);p.setLong(2,id);p.executeUpdate();}}
}
