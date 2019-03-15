//https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/edge/EdgeDriver.html
//https://www.guru99.com/select-option-dropdown-selenium-webdriver.html
package com.company;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Main {
    //Edge selenium automation
    private static EdgeDriverService service;
    private static WebDriver driver;
    private static JavascriptExecutor javascriptExecutor;
    private static void createAndStartService() {
        try {
            service = new EdgeDriverService.Builder()
                    .usingDriverExecutable(new File("C:\\Windows\\SysWOW64\\MicrosoftWebDriver.exe"))
                    .usingAnyFreePort()
                    .build();
            service.start();
        }
        catch (IOException e) {
            //
        }
    }
    private static void init() {
        createAndStartService();
        driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.edge());
        javascriptExecutor = (JavascriptExecutor)driver;
    }

    //general utility
    private static void print(String toPrint){
        System.out.print(toPrint);
    }
    private static void print(int toPrint){
        System.out.print(toPrint);
    }
    private static void sleep(int milliSeconds){
        try {
            Thread.sleep(milliSeconds);
        }
        catch (InterruptedException e) {
            //
        }
    }
    private static boolean isEmptyFile(String source) {
        try {
            for (String line : Files.readAllLines(Paths.get(source))) {
                if (line != null && !line.trim().isEmpty()) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Default to true.
        return true;
    }
    private static void removeFirstLineOfFile(String fileName) {
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            //Initial write position
            long writePosition = raf.getFilePointer();
            raf.readLine();
            // Shift the next lines upwards.
            long readPosition = raf.getFilePointer();

            byte[] buff = new byte[1024];
            int n;
            while (-1 != (n = raf.read(buff))) {
                raf.seek(writePosition);
                raf.write(buff, 0, n);
                readPosition += n;
                writePosition += n;
                raf.seek(readPosition);
            }
            raf.setLength(writePosition);
            raf.close();
        } catch (IOException e) { print("unable to remove first line of file!\n"); }
    }
    private static void appendToEndOfFile (String fileName, String line) {
        try {
            Files.write(Paths.get(fileName), line.getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(fileName), "\n".getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) { print("unable to append to file!\n"); }
    }
    private static void removeFileEntry(String fileName, String line) {
        String currentLine;
        File inputFile = new File(fileName);
        File tempFile = new File("temp" + fileName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if(trimmedLine.equals(line)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            inputFile.delete();
            print("removed " + line + "\n");
            boolean successful = tempFile.renameTo(inputFile);
        } catch (IOException e) { print("Failed to remove entry!\n"); }
    }
    private static ArrayList<String> loadFile (String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(fileName));
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        return lines;
    }
    private static void removeAllDuplicates(String fileName) { //if there is more than just one duplicate then this will not remove that... This could be optimized
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            Set<String> lines = new HashSet<String>(10000); // maybe should be bigger
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            for (String unique : lines) {
                writer.write(unique);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) { print("failed to remove duplicate from file\n"); }
    }

    //Progress display demo
    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    private static void drawProgressBar(int progressPercentage){
        if ((progressPercentage <= 100)&&(progressPercentage >= 0)) {
            int positiveProgress = ((progressPercentage + 10 - 1) / 10);
            for (int i = 0; i < positiveProgress; i++)
                System.out.print('X');
            for (int i = 0; i < 10 - positiveProgress; i++)
                System.out.print('.');
        }
        else {
            System.out.print("INVALID PROGRESS PERCENTAGE!");
        }
    }
    private static void drawProgress(int progressPercentage, long timeStart, String title) {
        clearConsole();
        System.out.print(title);
        System.out.print(" Progress: ");
        System.out.print(rotateRotator());
        drawProgressBar(progressPercentage);



    }
    private static char rotateRotator(){
        Random random = new Random();
        switch (random.nextInt(11)) {
            case 0:
                return ')';
            case 1:
                return '!';
            case 2:
                return '@';
            case 3:
                return '#';
            case 4:
                return '$';
            case 5:
                return '%';
            case 6:
                return '^';
            case 7:
                return '&';
            case 8:
                return '*';
            case 9:
                return '(';
            default:
                return '~';
        }
    }

    //General selenium
    private static int getPageHeight() {
        return Integer.parseInt(javascriptExecutor.executeScript("return document.body.scrollHeight").toString());
    }
    private static int getCurrentPageScrollPosition() {
        return Integer.parseInt(javascriptExecutor.executeScript("return document.body.scrollTop").toString());
    }

    //Pocket scraper
    private static void saveArticleList(ArrayList<String> articles) {
        //save the remainder of the list so that cookies and browser can be cleared thus resuming where left off
        //appends to end of file
        for (String url: articles) {
            appendToEndOfFile("urlsToScrape.txt", url);
        }
        /*PrintWriter printWriter = null;
        try{
            printWriter = new PrintWriter("urlsToScrape.txt", "UTF-8");
        } catch (IOException e) {}
        for (String url: articles) {
            if (printWriter != null) {
                printWriter.print(url);
                printWriter.print("#\n");
                //printWriter.print();//title
            }
        }
        printWriter.close(); //have to call this once I am done with everything else it will be a blank file*/
    }
    private static void getArticles(ArrayList<String> urls) {
        boolean isFirstRun = true;
        while (getCurrentPageScrollPosition() < getPageHeight()) {
            if ((getCurrentPageScrollPosition() > getPageHeight() - 2000) && (!isFirstRun))//The -2000 is an offset for the inability for the page scroll to go to "the bottom"
                break;
            isFirstRun = false;

            System.out.print("visibleurls size: ");
            System.out.println(urls.size());
            System.out.println("\n");

            getVisibleArticles(urls);
            //loadVisibleArticles(visiblePocketUrls);
            //loadArticles(visiblePocketUrls);
            //scrapeLoadedArticles(realUrls, visiblePocketTitles);

            //ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
            //driver.switchTo().window(tabs.get(0)); //have to manually switch back because it loses focus
            // This  will scroll down the page by 1000 pixel vertical
            javascriptExecutor.executeScript("window.scrollBy(0,1000)");
        }
    }
    private static void getVisibleArticles(ArrayList<String> urls) {
        String tempUrl;
        List<WebElement> visiblePocketItems = driver.findElements(By.className("css-7zhfhb"));
        for (WebElement element: visiblePocketItems) {
            tempUrl = element.findElement(By.cssSelector("a")).getAttribute("href");
            if (!urls.contains(tempUrl))
                urls.add(tempUrl);
        }
    }
    private static void loadArticles(ArrayList<String> urls) {
        String tempEntry;
        List<WebElement> title;
        List<WebElement> loadOriginalLink; //Have to search for all elements, just using findElement will throw an exception if the element doesnt exist
        for(String url: urls) {
            ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
            driver.switchTo().window(tabs.get(1)); //Should exist if this function was called by loadVisibleArticles()
            driver.get(url);
            print("loading\n");
            removeFileEntry("urlsToScrape.txt", url);
            if (!(tabs.size() > driver.getWindowHandles().size())) { //Check to see if a new tab didn't load
                loadOriginalLink = driver.findElements(By.className("css-1gitlr5"));
                title = driver.findElements(By.className("css-1iw3xtd"));
                if (loadOriginalLink.size() != 0) { //In scrapeLoadedArticles(), these pages will be treated the same as root pages thus we have to scrape here, otherwise it will just be closed
                    tempEntry = loadOriginalLink.get(0).getAttribute("href") + " " + title.get(0).getText();
                    appendToEndOfFile( "scrapedLinks.txt", tempEntry);
                }
                else {
                    print("page didnt load! \n");
                    appendToEndOfFile( "urlsToScrape.txt", url);
                    //page didnt load because pocket has blocked the scraping?
                    //clear history
                    //login
                    //break
                }
            }
        }
    }
    private static void loadVisibleArticles(ArrayList<String> urls) {
        //Open a new tab to preserve the pocket root tab
        ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
        javascriptExecutor.executeScript("window.open()");
        loadArticles(urls);
    }
    private static void scrapeLoadedArticles() {
        ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
        for(int tab = 1; tab <= tabs.size()-1; tab++) { //start at second tab (first tab after root tab)
            driver.switchTo().window(tabs.get(tab));
            if (!Pattern.compile(Pattern.quote("app.getpocket.com"), Pattern.CASE_INSENSITIVE).matcher(driver.getCurrentUrl()).find()) { //this is getting called twice for some reason and is making duplicates. Will just remove duplicates at end.
                appendToEndOfFile( "scrapedLinks.txt", driver.getCurrentUrl() + " " + driver.getTitle());
                print("page scraped! " + driver.getCurrentUrl() + "\n");
                driver.close();
            }
        }
    }
    private static void scrapePocketArticles() {
        ArrayList<String> visiblePocketUrls = new ArrayList<>();
        ArrayList<String> urlsToScrape = new ArrayList<>();
        driver.get("https://app.getpocket.com/");
        if (isEmptyFile("urlsToScrape.txt")) {
            getArticles(visiblePocketUrls);  //I cant do everything in one loop because pocket has an anti scraping feature that stops redirects after some amount of calls so I have to save progress and restart when that happens
            saveArticleList(visiblePocketUrls);
        }
        while (!isEmptyFile("urlsToScrape.txt")) {
            visiblePocketUrls = loadFile("urlsToScrape.txt");
            for (int entry = 0; entry < 15; entry++ ) { //scrape in sets of 15 as to not bog down the browser with tabs
                urlsToScrape.add(visiblePocketUrls.get(entry));
                print(urlsToScrape.get(entry));
                print("\n");
                removeFirstLineOfFile("urlsToScrape.txt");
            }
            loadVisibleArticles(urlsToScrape);
            scrapeLoadedArticles();
            urlsToScrape.clear();
            removeAllDuplicates("scrapedLinks.txt");
        }
    }
    private static void gatherChangedWorkItems (String ownersName, String query) {
        WebDriverWait wait = new WebDriverWait(driver, 500);
        driver.get(query);
        driver.manage().window().maximize();
        ArrayList<String> allWorkItemUrls = new ArrayList<>();
        ArrayList<String> changedWorkItems = new ArrayList<>();
        int queryCount = Integer.parseInt(driver.findElement(By.className("vss-HubTextTile--primaryText")).getText().replaceAll("[^\\d.]", ""));
        System.out.println(queryCount);
        //Get all the work items
        while (allWorkItemUrls.size() < queryCount){
            List<WebElement> visibleWorkItems = driver.findElements(By.className("work-item-title-link"));
            for(WebElement workItemWebElement: visibleWorkItems)
                if(!allWorkItemUrls.contains(workItemWebElement.getAttribute("href")))
                    allWorkItemUrls.add(workItemWebElement.getAttribute("href"));
            /*
            //scroll down
            WebElement queryGrid = driver.findElement(By.className("grid-canvas"));
            queryGrid.sendKeys(Keys.ARROW_DOWN);
            */
            //Zoom out
            WebElement html = driver.findElement(By.tagName("html"));
            html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
        }
        //Check all the work items
        for (String workItem: allWorkItemUrls){
            driver.get(workItem);
            WebElement lastCommentor = driver.findElement(By.className("discussion-messages-user"));
            System.out.println(lastCommentor.getText());
            if (!lastCommentor.getText().equals(ownersName))
                changedWorkItems.add(workItem);
        }
        for (String changedItem: changedWorkItems)
            System.out.println(changedItem);
        /*
        //Save work items
        try {
            FileWriter fileWriter = new FileWriter(ownersName + "ChangedItems");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (String changedItem: changedWorkItems) {
                System.out.println(changedItem);
                printWriter.print(changedItem);
            }
        }
        catch (IOException e) {
            //
        }
        */
    }
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        init();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        //gatherChangedWorkItems("Zachariah Leonard", "https://microsoft.visualstudio.com/OS/_queries/query/7e77b757-aaad-40a2-801e-06700818e43a/");
        scrapePocketArticles();
        long finish = System.currentTimeMillis();
        System.out.print("Elapsed time: ");
        long elapsedTime = finish - start;
        System.out.println(elapsedTime);
        //driver.quit();
        service.stop();
    }
}
