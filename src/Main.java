import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        LoginPrompt();
    }
    private static void  LoginPrompt() {
        System.out.println("Enter the number of the action you want to execute\n1.Login\n2.Terminate Program");
        Scanner s = new Scanner(System.in);
        int actionNum =  s.nextInt();
        s.nextLine(); // Consume the newline character left by nextInt()
        switch (actionNum)
        {
            case 1:
                System.out.println("Enter Username");
                String username = s.nextLine();
                System.out.println("Enter Password");
                String password = s.nextLine();
                try {
                    FilmReport f = new FilmReport(username,password);
                    ActionPrompt(f);
                }
                catch (Exception e){
                    e.printStackTrace();

                }

                break;
            case 2:
                return;
            default:
                System.out.println("Invalid input terminating process due to user fault");
                return;
        }
        System.out.println("\033[H\033[J");
        System.out.flush();

    }
    private static void ActionPrompt(FilmReport f){
        if(f==null)
            throw new  NullPointerException();
        System.out.println("Enter the number of the action you want to execute\n1.Get Movies By Actor\n2.Get Similar Movies\n3.Logout and Terminate Process");
        Scanner s = new Scanner(System.in);
        int optionNumber = s.nextInt();
        s.nextLine();
        switch (optionNumber)
        {
            case 1:
                System.out.println("Enter  Actor first Name ");
                String firstName = s.nextLine();
                System.out.println("Enter  Actor last Name ");
                String lastName = s.nextLine();
                try {
                    String[] arr = f.getFilmsByActor(firstName,lastName);
                    if(arr.length==0){
                        System.out.println("No movies found");

                    }
                    else{
                        for (int i = 0; i < arr.length; i++) {
                            System.out.println(arr[i]);
                        }
                    }

                }
                catch (Exception e){
                    e.getMessage();
                    e.printStackTrace();
                }

                break;
            case 2:
                try{
                    String[] arr= enterTitles();
                    arr= f.getSimilarTitles(arr);
                    if(arr.length==0){
                        System.out.println("No movies found");

                    }
                    else{
                        for (int i = 0; i < arr.length; i++) {
                            System.out.println(arr[i]);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("Something went wrong");
                }
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid input terminating process due to user fault");
                break;
        }

    }
    private static String[] enterTitles() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter titles separated by spaces: ");
        String input = scanner.nextLine(); // Read the whole line of input

        // Split the input based on spaces
        String[] titles = input.split("\\s+");

        scanner.close();
        return titles;
    }
}