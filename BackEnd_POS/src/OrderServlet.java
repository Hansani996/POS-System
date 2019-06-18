import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(urlPatterns = "/order")
public class OrderServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JsonReader reader= Json.createReader(req.getReader());
        resp.setContentType("application/json");
        PrintWriter pw=resp.getWriter();
        Connection conn=null;

       try {
           conn=ds.getConnection();
           conn.setAutoCommit(false);
           JsonObject order =reader.readObject();
           String oid=order.getString("oid");
           String date=order.getString("date");
           String customerId=order.getString("customerId");



           JsonObject orderDetail =reader.readObject();
           String orderId=orderDetail.getString("orderId");
           String itemCode=orderDetail.getString("itemCode");
           int qty=Integer.parseInt(orderDetail.getString("qty"));
           double price=Double.parseDouble(orderDetail.getString("Price"));
           conn=ds.getConnection();


           PreparedStatement pstm=conn.prepareStatement("INSERT INTO orders VALUES (?,?,?)");
           pstm.setObject(1,oid);
           pstm.setObject(2,date);
           pstm.setObject(3,customerId);
           boolean result=pstm.executeUpdate()>0;


           PreparedStatement stm=conn.prepareStatement("INSERT INTO itemdetail VALUES (?,?,?,?)");
           stm.setObject(1,orderId);
           stm.setObject(2,itemCode);
           stm.setObject(3,qty);
           stm.setObject(4,price);


       }catch (SQLException e){
           e.printStackTrace();
       }finally {
         try {
             conn.close();
         }catch (Exception e){

         }
       }
    }
}
