import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmReport {
    final static  String DB_URL = "jdbc:mysql://localhost:3306/sakila";
    private String username;
    private  String password;
    Connection connection;

    public FilmReport(String username, String password) throws Exception
    {
        this.username=username;
        this.password=password;
        try {
             connection = DriverManager.getConnection(DB_URL,this.username,this.password);

        }
        catch (Exception e)
        {
          throw new UnknownError("Connection to DB failed");
        }
        createTables();
    }
    public void createTables(){
        try (Statement statement = connection.createStatement()) {

            // Create actor table
            String createActorTableSQL = "CREATE TABLE actor (" +
                    "actor_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "first_name VARCHAR(50) NOT NULL," +
                    "last_name VARCHAR(50) NOT NULL)";

            statement.executeUpdate(createActorTableSQL);

            // Create film table
            String createFilmTableSQL = "CREATE TABLE film (" +
                    "film_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL)";

            statement.executeUpdate(createFilmTableSQL);

            System.out.println("Tables created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] getFilmsByActor(String firstName,String lastName) throws Exception
    {
        List<String> films = new ArrayList<>();
        /*
        * SQL query obtained using chatGPT
        * */
        final String query =  "SELECT film.title " +
                "FROM film " +
                "INNER JOIN film_actor ON film.film_id = film_actor.film_id " +
                "INNER JOIN actor ON film_actor.actor_id = actor.actor_id " +
                "WHERE actor.first_name = ? AND actor.last_name = ? " +
                "ORDER BY film.title ASC";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,firstName);
            statement.setString(2,lastName);
            ResultSet set = statement.executeQuery();
            while (set.next()){
                String filmTitle = set.getString("title");
                films.add(filmTitle);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Convert List<String> to String[]
        String[] filmsArray = new String[films.size()];
        return films.toArray(filmsArray);
    }
    /*
    *
    public String[] getSimilarTitles( String[]){

    }
    */
    private String WordWithFiveLetters(String s){
        if(s.length()<5){
            System.out.println("String s has a length lower than 5 at: WordWithFiveLetters");
            return "";
        }
        String word="";
        for(int i=0; i<s.length();i++){
            char ch = s.charAt(i);
            if(!(Character.isDigit(ch) || Character.isWhitespace(ch)) && word.length()<5){
                word+=ch;
            }
            else {
                word="";
                continue;
            }
        }
        return  word;
    }


}