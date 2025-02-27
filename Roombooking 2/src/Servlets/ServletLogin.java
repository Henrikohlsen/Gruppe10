package Servlets;

import Tools.DbFunctionality;
import Tools.DbTool;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles Login from index.html and parses String parameters to {@code DbFunctionality} to check for
 * {@code String} is existing in Database.
 *
 * @author trym, brisdalen
 * @see Servlets.AbstractServlet
 * @see javax.servlet.http.HttpServlet
 * @see javax.servlet.GenericServlet
 * @see DbFunctionality
 * @see DbTool
 */

@WebServlet(name = "Servlets.ServletLogin", urlPatterns = {"/Servlets.ServletLogin"})
public class ServletLogin extends AbstractServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            System.out.println("Login attempt started");
            //prints start of html tags.
            printNav(out);
            //gets the username -> email in this case.
            String userName = request.getParameter("loginemail").toLowerCase();
            String lowercaseUsername = userName.toLowerCase();
            //gets the users password.
            String password = request.getParameter("loginpassword");
            //gets the form value from action
            String action = request.getParameter("action").toLowerCase();

            if (action.contains("login")) {
                DbTool dbTool = new DbTool();
                //Establishes connection to database
                Connection connection = dbTool.dbLogIn(out);
                DbFunctionality dbFunctionality = new DbFunctionality();
                //checks whether the users Email matches the stored password
                if (dbFunctionality.checkUser(lowercaseUsername, password, connection)) {
                    // If successful login TODO: make it pop-up
                    System.out.println("success!");
                    out.print("Welcome " + lowercaseUsername + "!");
                    out.print("<br>");
                    //redirects the user to the loggedIn.html
                    ServletContext servletContext = getServletContext();
                    HttpSession session = request.getSession();
                    session.setAttribute("userEmail", userName);
                    /*
                    f.eks:  sjekk UserType i database utifra brukernavn
                            if(user.userType.equals("admin") {
                                servletContext.getRequestDispatcher("/loggedInAdmin.html").forward(request, response);
                            } else {
                                servletContext.getRequestDispatcher("/loggedInDefault.html").forward(request, response);
                            }
                     NB: Ikke gjøre det mulig å komme til admin-siden ved kun URL eller med parameter
                     */
                    session.setAttribute("userEmail", userName);
                    servletContext.getRequestDispatcher("/loggedIn.html").forward(request, response);
                    //if the login fails
                    try {
                        System.out.println("attempting to close");
                        connection.close();
                        if (!connection.isClosed()) {
                            System.out.println("connection is not closed ");
                        } else {
                            System.out.println("connection is closed");
                        }
                        out.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("connection failed" + e);
                    }
                } else {
                    // If not TODO: Add out.print error message for wrong password vs email
                    if (connection.isClosed()) {
                        System.out.println("connection closed");
                    } else {
                        connection.close();
                        System.out.println("connection not closed");
                    }
                    System.out.println("fail");
                    out.println("Sorry, we do not recognize \"" + lowercaseUsername + "\".");
                    out.print("<br>");

                }
                //adds a return button if the login fails.
                addHomeButton(out);
            }
            //prints script to establish connection between bootstrap and html
            addBootStrapFunctionality(out);
            out.print("</body>");
            out.print("</html>");
            //prints errors: if the database fails, if the password is wrong.
        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
