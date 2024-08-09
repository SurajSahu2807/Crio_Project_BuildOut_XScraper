package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

   @Test(enabled = true,priority = 1) 
   public void testCase01(){
        System.out.println("Start TestCase01");
        driver.get("https://www.scrapethissite.com/pages/");
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(20));
        WebElement WebTableTopicLink = driver
                .findElement(By.xpath("//a[text()='Hockey Teams: Forms, Searching and Pagination']"));
        WebTableTopicLink.click();
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='hockey']/div/div[1]/div/h1")));
           
          // List to hold the team data
          ArrayList<HashMap<String, Object>> teamDataList = new ArrayList<>();
       try{

            for(int i=1;i<=4;i++){
                WebElement table = driver.findElement(By.xpath("//*[@class='table']"));
                List<WebElement> rows = table.findElements(By.xpath(".//tr[@class='team']"));

                //Iterate over each row and extract data
                for(WebElement row : rows){
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                     if (cells.size() > 0) { // If there are cells in the row
                        String teamName = cells.get(0).getText();
                        String year = cells.get(1).getText();
                        double winPercentage = Double.parseDouble(cells.get(5).getText());

                        if (winPercentage < 0.40) {
                            HashMap<String, Object> teamData = new HashMap<>();
                            teamData.put("Epoch Time of Scrape", Instant.now().getEpochSecond());
                            teamData.put("Team Name", teamName);
                            teamData.put("Year", year);
                            teamData.put("Win %", winPercentage);
                            teamDataList.add(teamData);
                        }
                    }
                }

                // Go to the next page (Assuming a "Next" button)
                WebElement nextButton = driver.findElement(By.xpath("//a[@aria-label='Next']"));
                nextButton.click();
            }

            convertToJsonAndSave1(teamDataList);
            testFileExistsAndIsNotEmpty();
            System.out.println("End TestCase01");


       }catch(Exception e){
            e.printStackTrace();
       }
        
   }

    public static void convertToJsonAndSave1(ArrayList<HashMap<String, Object>> teamDataList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File outputFile = new File("output/hockey-team-data.json");
            outputFile.getParentFile().mkdirs(); // Create directories if they don't exist
            mapper.writeValue(outputFile, teamDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void testFileExistsAndIsNotEmpty() {
        File file = new File("output/hockey-team-data.json");
        Assert.assertTrue(file.exists(), "File does not exist.");
        Assert.assertTrue(file.length() > 0, "File is empty.");
    }

    @Test(enabled=true,priority = 2)
    public void testCase02(){
        System.out.println("Start TestCase02");
        driver.get("https://www.scrapethissite.com/pages/");
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(20));
        WebElement WebTableTopicLink = driver
                .findElement(By.xpath("//a[text()='Oscar Winning Films: AJAX and Javascript']"));
        WebTableTopicLink.click();
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='oscars']/div/div[1]/div/h1")));
        
     // List to hold the movie data
     ArrayList<HashMap<String, Object>> movieDataList = new ArrayList<>();

     try {
       
         // Find all the years (assuming they're links or buttons)
         List<WebElement> years = driver.findElements(By.xpath("//a[@href='#']")); // Adjust selector

         // Iterate through each year
         for (WebElement yearElement : years) {
             String year = yearElement.getText();
             yearElement.click(); // Click on the year to view the movies

             // Wait for the page to load if necessary
             wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//tbody[@id='table-body']//tr[@class='film']")));

             // Find the top 5 movies (adjust the selector accordingly)
             List<WebElement> movies = driver.findElements(By.xpath("//tbody[@id='table-body']//tr[@class='film']"));

             // Ensure the list has enough elements
                if (movies.size() > 4) {
                    movies = movies.subList(0, 5); // This will safely create a sublist of elements at indices 1, 2, and 3
                } else {
                    System.out.println("Not enough movies found. Found " + movies.size() + " movies.");
                    // Handle the scenario when there are fewer elements
                }
             // Iterate through the movies and extract the data
             for (int i = 0; i < movies.size(); i++) {
                 WebElement movie = movies.get(i); 
                 HashMap<String, Object> movieData = new HashMap<>();

                 String title = movie.findElement(By.xpath(".//td[@class='film-title']")).getText();
                 String nomination = movie.findElement(By.xpath(".//td[@class='film-nominations']")).getText();
                 String awards = movie.findElement(By.xpath(".//td[@class='film-awards']")).getText();

                 movieData.put("Epoch Time of Scrape", Instant.now().getEpochSecond());
                 movieData.put("Year", year);
                 movieData.put("Title", title);
                 movieData.put("Nomination", nomination);
                 movieData.put("Awards", awards);

                 // Mark the first movie as the winner (isWinner = true), and the rest as (isWinner = false)
                 movieData.put("isWinner", i == 0);

                 movieDataList.add(movieData);
             }

            //  driver.navigate().back(); // Go back to the year selection page
         }
         System.out.println("End TestCase02");
     }catch(Exception e){
        e.printStackTrace();
     }

     // Convert the data to JSON and save
     convertToJsonAndSave2(movieDataList);
        
    }

    public static void convertToJsonAndSave2(ArrayList<HashMap<String, Object>> movieDataList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File outputFile = new File("output/oscar-winner-data.json");
            outputFile.getParentFile().mkdirs(); // Create directories if they don't exist
            mapper.writeValue(outputFile, movieDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }
}