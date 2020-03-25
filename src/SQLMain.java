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
                String cName= resultSet.getString("customer_name");
                if(cName!=null){
                    customerName.appendChild(document.createTextNode(cName));
                }

                Element addressClass = document.createElement("address");
                Element streetAddress = document.createElement("street_address");
                String sAddress = resultSet.getString("street_address");
                if(sAddress!=null){
                    streetAddress.appendChild(document.createTextNode(sAddress));
                }

                Element city = document.createElement("city");
                String cityString = resultSet.getString("city");
                if(cityString!=null) {
                    city.appendChild(document.createTextNode(cityString));
                }

                Element region = document.createElement("region");
                String region_Check2= resultSet.getString("region");
                if(region_Check2!=null){
                    region.appendChild(document.createTextNode(region_Check2));
                }

                Element postalCode = document.createElement("postal_code");
                String pCode = resultSet.getString("postal_code");
                if(pCode!=null) {
                    postalCode.appendChild(document.createTextNode(pCode));
                }

                Element country = document.createElement("country");
                String countryString = resultSet.getString("country");
                if(countryString!=null){
                    country.appendChild(document.createTextNode(countryString));
                }

                addressClass.appendChild(streetAddress);
                addressClass.appendChild(country);
                addressClass.appendChild(city);
                addressClass.appendChild(postalCode);
                addressClass.appendChild(region);

                Element numOrders = document.createElement("num_orders");
                String nOrders = resultSet.getString("num_orders");
                if(nOrders!=null){
                    numOrders.appendChild(document.createTextNode(nOrders));
                }


                Element orderValue = document.createElement("order_value");
                String oValue = resultSet.getString("order_value");
                if(oValue!=null) {
                    orderValue.appendChild(document.createTextNode(oValue));
                }

                customerClass.appendChild(customerName);
                customerClass.appendChild(addressClass);
                customerClass.appendChild(numOrders);
                customerClass.appendChild(orderValue);

            }

            statement2.executeQuery("use " + dbName + ";");
            resultSet2 = statement2.executeQuery(query2);

            while (resultSet2.next()){
                Element categoryClass = document.createElement("category");
                productList.appendChild(categoryClass);

                Element categoryName = document.createElement("category_name");
                String cName = resultSet2.getString("category_name");
                if(cName!=null)
                {
                    categoryName.appendChild(document.createTextNode(cName));
                }

                Element productClass = document.createElement("product");
                Element productName = document.createElement("ProductName");

                String pName = resultSet2.getString("product_name");
                if(pName!=null){
                    productName.appendChild(document.createTextNode(pName));
                }

                Element supplierName = document.createElement("supplier_name");
                String sName = resultSet2.getString("supplier_name");
                if(sName!=null) {
                    supplierName.appendChild(document.createTextNode(sName));
                }

                Element numProducts = document.createElement("num_products");
                String nProducts = resultSet2.getString("num_products");
                if(nProducts!=null) {
                    numProducts.appendChild(document.createTextNode(nProducts));
                }

                Element productValue = document.createElement("product_value");
                String pValue = resultSet2.getString("product_value");
                if(pValue!=null) {
                    productValue.appendChild(document.createTextNode(pValue));
                }

                productClass.appendChild(productName);
                productClass.appendChild(supplierName);
                productClass.appendChild(numProducts);
                productClass.appendChild(productValue);

                categoryClass.appendChild(categoryName);
                categoryClass.appendChild(productClass);


            }

            statement3.executeQuery("use " + dbName + ";");
            resultSet3 = statement3.executeQuery(query3);

            while (resultSet3.next()){

                Element supplierClass = document.createElement("supplier");
                supplierList.appendChild(supplierClass);

                Element supplierName = document.createElement("supplier_name");
                String sName = resultSet3.getString("supplier_name");
                if(sName!=null) {
                    supplierName.appendChild(document.createTextNode(sName));
                }

                Element addressClass = document.createElement("address");
                Element streetAddress = document.createElement("street_address");
                String sAddress = resultSet3.getString("street_address");
                if(sAddress!=null) {
                    streetAddress.appendChild(document.createTextNode(sAddress));
                }

                Element city = document.createElement("city");
                String cityAddress = resultSet3.getString("city");
                if(cityAddress!=null) {
                    city.appendChild(document.createTextNode(cityAddress));
                }

                Element region = document.createElement("region");
                String region_Check= resultSet3.getString("region");
                if(region_Check!=null) {
                    region.appendChild(document.createTextNode(region_Check));
                }

                Element postalCode = document.createElement("postal_code");
                String pCode = resultSet3.getString("postal_code");
                if(pCode!=null) {
                    postalCode.appendChild(document.createTextNode(pCode));
                }

                Element country = document.createElement("country");
                String countryAddress = resultSet3.getString("country");
                if(countryAddress!=null) {
                    country.appendChild(document.createTextNode(countryAddress));
                }

                addressClass.appendChild(streetAddress);
                addressClass.appendChild(city);
                addressClass.appendChild(region);
                addressClass.appendChild(postalCode);
                addressClass.appendChild(country);

                Element numProducts = document.createElement("num_products");
                String nProducts = resultSet3.getString("num_products");
                if(nProducts!=null) {
                    numProducts.appendChild(document.createTextNode(nProducts));
                }

                Element productValue = document.createElement("product_Value");
                String pValue = resultSet3.getString("product_Value");
                if(pValue!=null) {
                    productValue.appendChild(document.createTextNode(pValue));
                }

                supplierClass.appendChild(supplierName);
                supplierClass.appendChild(addressClass);
                supplierClass.appendChild(numProducts);
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