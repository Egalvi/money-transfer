package com.test;

import com.test.service.AccountRestService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;

public class App {
    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8081);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                AccountRestService.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

//    public static void main(String[] args) throws Exception {
//
//        String connectionString = "jdbc:h2:mem:accounts;INIT=RUNSCRIPT FROM 'classpath:init.sql'";
//        JdbcConnectionPool pool = JdbcConnectionPool.create(connectionString, "sa", "sa");
//        try (Connection con = pool.getConnection();
//             Statement stm = con.createStatement();
//             ResultSet rs = stm.executeQuery("SELECT * from account")) {
//
//            while (rs.next()) {
//                System.out.println(rs.getString(1) + ", " + rs.getString(2));
//            }
//
//        }
//    }
}
