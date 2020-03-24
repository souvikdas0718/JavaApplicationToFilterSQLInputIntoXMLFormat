import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

public class SQLMain {

    public String isValidDate(String date){
        String sdate="";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        try {
            formatter.parse(date);
            return date;
        } catch (Exception e) {
            System.out.println("Invalid Date");
            Scanner scanner = new Scanner(System.in);
            date = scanner.next();
            sdate =isValidDate(date);
        }
        return sdate;
    }

    public String isValidFileName(String fName){
        String fileName="";
        if(fName == null){
            System.out.println("File Name cannot be empty");
            Scanner scanner = new Scanner(System.in);
            fName = scanner.nextLine();
            fileName =isValidFileName(fName);
        }else if(fName.isEmpty()){
            System.out.println("File Name cannot be empty");
            Scanner scanner = new Scanner(System.in);
            fName = scanner.nextLine();
            fileName =isValidFileName(fName);
        }
        else{
            return fName;
        }
        return fileName;
    }

    public static void main(String[] args) {

        //String startDate = "1997-10-01";
        //String endDate = "1997-12-31";

        SQLMain sqlMain = new SQLMain();
        System.out.print("Enter Start Date: ");
        Scanner scanner = new Scanner(System.in);
        String sDate = scanner.next();
        String startDate =sqlMain.isValidDate(sDate);

        //System.out.println("Start Date "+startDate);
        System.out.print("Enter End Date: ");
        String eDate= scanner.next();
        String endDate =sqlMain.isValidDate(eDate);

        System.out.println("Start Date "+startDate+" End Date "+endDate);

        System.out.print("Enter File Name: ");
        String fName= scanner.next();
        fName=fName.trim();
        String fileName= sqlMain.isValidFileName(fName);
        System.out.println("FileName "+fileName);

        ///Users/souvikdas/MACS/Assignmnet5/SQLOutput.xml





        // Variables for the connections and the queries.
        Connection connect = null;      // the link to the database
        Statement statement = null;     // a place to build up an SQL query
        Statement statement2 = null;     // a place to build up an SQL query
        Statement statement3 = null;     // a place to build up an SQL query
        ResultSet resultSet = null;     // a data structure to receive results from an SQL query
        ResultSet resultSet2 = null;     // a data structure to receive results from an SQL query
        ResultSet resultSet3 = null;     // a data structure to receive results from an SQL query
        // Info used for the connection that you will definitely want to change, make
        // into parameters, or draw from environment variables instead of having hard-coded

        Properties identity = new Properties();  //  Using a properties structure, just to hide info from other users.
        MyIdentity me = new MyIdentity();        //  My own class to identify my credentials.  Ideally load Properties from a file instead and this class disappears.

        String user;
        String password;
        String dbName;
        String query = "";
        String query2 = "";
        String query3 = "";

        // Get the info for logging into the database.

        me.setIdentity(identity);                   // Fill the properties structure with my info.  Ideally, load properties from a file instead to replace this bit.
        user = identity.getProperty("user");
        password = identity.getProperty("password");
        dbName = identity.getProperty("database");

        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC&useSSL=false", user, password);

            // Statements allow to issue SQL queries to the database.  Create an instance
            // that we will use to ultimately send queries to the database.
            statement = connect.createStatement();
            statement2 = connect.createStatement();
            statement3 = connect.createStatement();

            // Choose a database to use

            query = "with temp as (select OrderID, sum((orderdetails.UnitPrice - orderdetails.Discount) * orderdetails.Quantity) as OrderValue from orderdetails group by OrderID)" +
                    " select customers.ContactName as customer_name, customers.Address as street_address, customers.City as city, customers.Region as region," +
                    " customers.PostalCode as postal_code, customers.Country as country, count(customers.CustomerID) as num_orders, sum(temp.OrderValue) as order_value from temp" +
                    " join (select orders.CustomerID, orders.OrderID from orders where OrderDate between'"+ startDate +"' and '"+endDate +"') as newOrders on temp.OrderID = newOrders.OrderID" +
                    " join customers on customers.CustomerID = newOrders.CustomerID " +
                    "group by customers.CustomerID;";


            query2 = "with new_order as (select orders.OrderID from orders where OrderDate between'"+ startDate +"' and '"+endDate +"') " +
                    "select categories.categoryName as category_name, products.ProductName as product_name, suppliers.ContactName as supplier_name," +
                    " sum(orderdetails.Quantity) as num_products, (sum(orderdetails.Quantity) * orderdetails.UnitPrice) as product_Value from new_order " +
                    "join orderdetails on orderdetails.OrderID = new_order.OrderID " +
                    "join products on orderdetails.ProductID = products.ProductID " +
                    "join categories on products.CategoryID = categories.CategoryID " +
                    "join suppliers on products.SupplierID = suppliers.SupplierID " +
                    "group by products.ProductID;";


            query3 = "with new_order as (select orders.OrderID from orders where OrderDate between '"+ startDate +"' and '"+endDate +"')" +
                    "select suppliers.CompanyName as supplier_name, suppliers.Address as street_address, suppliers.City as city," +
                    "suppliers.Region as region, suppliers.PostalCode as postal_code, suppliers.Country as country, count(distinct orderdetails.ProductID) as num_products," +
                    "sum(orderdetails.UnitPrice * orderdetails.Quantity) as product_Value from new_order " +
                    "join orderdetails on orderdetails.OrderID = new_order.OrderID " +
                    "join products on orderdetails.ProductID = products.ProductID " +
                    "join suppliers on products.SupplierID = suppliers.SupplierID " +
                    "group by suppliers.SupplierID;";

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("year_end_summary");
            document.appendChild(root);

            Element year = document.createElement("year");
            root.appendChild(year);

            Element start_Date = document.createElement("start_date");
            start_Date.appendChild(document.createTextNode(startDate));
            year.appendChild(start_Date);

            Element end_Date = document.createElement("start_date");
            end_Date.appendChild(document.createTextNode(endDate));
            year.appendChild(end_Date);

            Element customerList = document.createElement("customer_list");
            root.appendChild(customerList);

            Element productList = document.createElement("product_list");
            root.appendChild(productList);

            Element supplierList = document.createElement("supplier_list");
            root.appendChild(supplierList);

            statement.executeQuery("use " + dbName + ";");
            resultSet = statement.executeQuery(query);

            while(resultSet.next()){

                Element customerClass = document.createElement("customer");
                customerList.appendChild(customerClass);

                Element customerName = document.createElement("customer_name");
                customerName.appendChild(document.createTextNode(resultSet.getString("customer_name")));
                customerClass.appendChild(customerName);

                Element addressClass = document.createElement("address");
                customerClass.appendChild(addressClass);

                Element streetAddress = document.createElement("street_address");
                streetAddress.appendChild(document.createTextNode(resultSet.getString("street_address")));
                addressClass.appendChild(streetAddress);

                Element city = document.createElement("city");
                city.appendChild(document.createTextNode(resultSet.getString("city")));
                addressClass.appendChild(city);

                Element region = document.createElement("region");
                region.appendChild(document.createTextNode(resultSet.getString("region")));
                addressClass.appendChild(region);

                Element postalCode = document.createElement("postal_code");
                postalCode.appendChild(document.createTextNode(resultSet.getString("postal_code")));
                addressClass.appendChild(postalCode);

                Element country = document.createElement("country");
                country.appendChild(document.createTextNode(resultSet.getString("country")));
                addressClass.appendChild(country);

                Element numOrders = document.createElement("num_orders");
                numOrders.appendChild(document.createTextNode(resultSet.getString("num_orders")));
                customerClass.appendChild(numOrders);

                Element orderValue = document.createElement("order_value");
                orderValue.appendChild(document.createTextNode(resultSet.getString("order_value")));
                customerClass.appendChild(orderValue);

            }

            statement2.executeQuery("use " + dbName + ";");
            resultSet2 = statement2.executeQuery(query2);

            while (resultSet2.next()){
                Element categoryClass = document.createElement("category");
                productList.appendChild(categoryClass);

                Element categoryName = document.createElement("category_name");
                categoryName.appendChild(document.createTextNode(resultSet2.getString("category_name")));
                categoryClass.appendChild(categoryName);

                Element productClass = document.createElement("product");
                categoryClass.appendChild(productClass);

                Element productName = document.createElement("ProductName");
                productName.appendChild(document.createTextNode(resultSet2.getString("product_name")));
                productClass.appendChild(productName);

                Element supplierName = document.createElement("supplier_name");
                supplierName.appendChild(document.createTextNode(resultSet2.getString("supplier_name")));
                productClass.appendChild(supplierName);

                Element numProducts = document.createElement("num_products");
                numProducts.appendChild(document.createTextNode(resultSet2.getString("num_products")));
                productClass.appendChild(numProducts);

                Element productValue = document.createElement("product_value");
                productValue.appendChild(document.createTextNode(resultSet2.getString("product_value")));
                productClass.appendChild(productValue);

            }

            statement3.executeQuery("use " + dbName + ";");
            resultSet3 = statement3.executeQuery(query3);

            while (resultSet3.next()){

                Element supplierClass = document.createElement("supplier");
                supplierList.appendChild(supplierClass);

                Element supplierName = document.createElement("supplier_name");
                supplierName.appendChild(document.createTextNode(resultSet3.getString("supplier_name")));
                supplierClass.appendChild(supplierName);

                Element addressClass = document.createElement("address");
                supplierClass.appendChild(addressClass);

                Element streetAddress = document.createElement("street_address");
                streetAddress.appendChild(document.createTextNode(resultSet3.getString("street_address")));
                addressClass.appendChild(streetAddress);

                Element city = document.createElement("city");
                city.appendChild(document.createTextNode(resultSet3.getString("city")));
                addressClass.appendChild(city);

                Element region = document.createElement("region");
                region.appendChild(document.createTextNode(resultSet3.getString("region")));
                addressClass.appendChild(region);

                Element postalCode = document.createElement("postal_code");
                postalCode.appendChild(document.createTextNode(resultSet3.getString("postal_code")));
                addressClass.appendChild(postalCode);

                Element country = document.createElement("country");
                country.appendChild(document.createTextNode(resultSet3.getString("country")));
                addressClass.appendChild(country);

                Element numProducts = document.createElement("num_products");
                numProducts.appendChild(document.createTextNode(resultSet3.getString("num_products")));
                supplierClass.appendChild(numProducts);

                Element productValue = document.createElement("product_Value");
                productValue.appendChild(document.createTextNode(resultSet3.getString("product_Value")));
                supplierClass.appendChild(productValue);

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            DOMSource domSource = new DOMSource(document);
            //StreamResult streamResult = new StreamResult(new File("./SQLOutput1.xml"));
            StreamResult streamResult = new StreamResult(new File(fileName));

            transformer.transform(domSource, streamResult);

            // Result set gets the result of the SQL query
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Always close connections, otherwise the MySQL database runs out of them.

            // Close any of the resultSet, statements, and connections that are open and holding resources.
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if(resultSet2!= null){
                    resultSet2.close();
                }
                if(resultSet3!= null){
                    resultSet3.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (statement2 != null) {
                    statement2.close();
                }
                if (statement3 != null) {
                    statement3.close();
                }
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
