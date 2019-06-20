import javax.annotation.Resource;
import javax.json.*;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter pw = resp.getWriter()) {

            if (req.getParameter("id") != null) {

                String id = req.getParameter("id");

                try {

                    Connection connection = ds.getConnection();
                    PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer WHERE id=?");
                    pstm.setObject(1, id);
                    ResultSet rst = pstm.executeQuery();

                    if (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("id", rst.getString(1));
                        ob.add("name", rst.getString(2));
                        ob.add("address",rst.getString(3));
                        System.out.println(ob);

                        resp.setContentType("application/json");
                        pw.println(ob.build());
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }


            //GET ALL CUSTOMER


            else{
                try (PrintWriter out = resp.getWriter()) {

                    resp.setContentType("application/json");

                    try {
                        Connection connection = ds.getConnection();

                        Statement stm = connection.createStatement();
                        ResultSet rst = stm.executeQuery("SELECT * FROM Customer");

                        JsonArrayBuilder customers = Json.createArrayBuilder();

                        while (rst.next()){
                            String id = rst.getString("id");
                            String name = rst.getString("name");
                            String address = rst.getString("address");

                            JsonObject customer = Json.createObjectBuilder().add("id", id)
                                    .add("name", name)
                                    .add("address", address)
                                    .build();
                            customers.add(customer);
                        }

                        out.println(customers.build().toString());

                        connection.close();
                    } catch (Exception ex) {
                        resp.sendError(500, ex.getMessage());
                        ex.printStackTrace();
                    }

                }
        }


    }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("working");
        JsonReader reader = Json.createReader(req.getReader());
        resp.setContentType("application/json");
        PrintWriter out=resp.getWriter();
        Connection conn=null;

        try {
            JsonObject customer =reader.readObject();
            System.out.println(customer);
            String id=customer.getString("id");
            String  name =customer.getString("name");
            String address=customer.getString("address");
            conn=ds.getConnection();

            PreparedStatement pstm=conn.prepareStatement("INSERT INTO Customer VALUES(?,?,?) ");
            pstm.setObject(1,id);
            pstm.setObject(2,name);
            pstm.setObject(3,address);
            boolean result =pstm.executeUpdate()>0;

            if (result){
                out.println("true");
            }else {
                out.println("flase");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            try {
                conn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        System.out.println("working...");

        if (id != null){

            try {
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("DELETE FROM customer WHERE id=?");
                System.out.println(id);
                pstm.setObject(1, id);
                int affectedRows = pstm.executeUpdate();
                if (affectedRows >  0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }catch (Exception ex){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ex.printStackTrace();
            }

        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getParameter("id") != null){

            try {
                JsonReader reader = Json.createReader(req.getReader());
                JsonObject customer = reader.readObject();

                String id = customer.getString("id");
                String name = customer.getString("name");
                String address = customer.getString("address");

                if (!id.equals(req.getParameter("id"))){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
//
//                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("UPDATE customer SET name=?, address=? WHERE id=?");
                pstm.setObject(3,id);
                pstm.setObject(1,name);
                pstm.setObject(2,address);
                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            }catch (JsonParsingException | NullPointerException  ex){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }catch (Exception ex){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }


        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}

