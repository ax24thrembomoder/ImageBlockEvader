package com.App;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.util.Properties;
import java.util.Random;
import java.io.IOException;

public class Changer {

    // configuration
    enum FilenameMethod {
        DEFAULT,
        ALPHANUMERIC
    }
    // default values if failed to read from app.properties
    private int MAX_FILES_TO_READ = 100;
    private int MINIMUM_RESOLUTION = 100;
    private int HEIGHT_LOWER_BOUND = 300;
    private int HEIGHT_UPPER_BOUND = 1500;
    private int WIDTH_LOWER_BOUND = 300;
    private int WIDTH_UPPER_BOUND = 2000;
    private FilenameMethod filenamemethod = FilenameMethod.DEFAULT;
    private Boolean RESIZE_IMAGES = Boolean.TRUE;
    private Boolean RENAME_IMAGES = Boolean.TRUE;

    private static final String alphanumeric_chars = "#@0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private ArrayList<File> files;
    private static Random random = new Random();

    private void readFilePaths(){
        files = new ArrayList<>();

        File f = new File(".");

        int c = 0;
        for(String pathname : f.list()){

            String extension = getExtension(pathname);

            // ignore wrong extensions
            if(!(extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") )){
                continue;
            }

            // break if too many files
            if(c >= MAX_FILES_TO_READ){
                break;
            }

            files.add(new File(pathname));
            System.out.println("method readFilePaths() --- file added"); // debug output
            c += 1;
        }

    }
    private void processImages() throws IOException{
        for(File f : files){
            if(RESIZE_IMAGES.equals(Boolean.TRUE)) {
                slightlyEditImg(f);
            }
            if(RENAME_IMAGES.equals(Boolean.TRUE)) {
                f = randomizeFilename(f, getExtension(f.getName()));
            }
        }

    }
    private void slightlyEditImg(File file) throws IOException{
        BufferedImage myPicture = null;
        boolean image_too_small = false;
        boolean image_too_big = false;
        try{
            myPicture = ImageIO.read(file);
        } catch(Exception e){
            System.out.println("failed to read file in slightlyEditImg()");
        }
        int h = myPicture.getHeight();
        int w = myPicture.getWidth();

        // sort things out relating to resolution
        if(h<MINIMUM_RESOLUTION || w <MINIMUM_RESOLUTION){
            throw new IOException("height or width too small");
        }
        else if(h < HEIGHT_LOWER_BOUND || w < WIDTH_LOWER_BOUND){
            image_too_small = true;
        } else if(h > HEIGHT_UPPER_BOUND || w > WIDTH_UPPER_BOUND){
            image_too_big = true;
        }



        // generate desired resize values and resize via  .resizeImage()
        double randommodifier = (((double) random.nextInt(100))/1000) + 0.01 ; // random modifier from 0.01 to 0.11

        if(  ((random.nextBoolean() == true || image_too_small))  && !image_too_big ){ //
            int desired_h = (int) ( (1.00+randommodifier)*(h));
            int desired_w = (int) ( (1.00+randommodifier)*(w));
            System.out.println("desired_h" +desired_h+" w "+desired_w);
            System.out.println("modifying by "+(1.00+randommodifier));
            myPicture = resizeImage(myPicture, desired_w, desired_h);

        } else{
            int desired_h = (int) ( (1.00-randommodifier)*(h) );
            int desired_w = (int) ( (1.00-randommodifier)*(w) );
            System.out.println("desired_h" +desired_h+" w "+desired_w);
            System.out.println("modifying by "+(1.00-randommodifier));
            myPicture = resizeImage(myPicture, desired_w, desired_h);

        }

        String fileName = file.getName();
        String extension = getExtension(fileName);

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        ImageIO.write(myPicture, extension, file);
    }
    private String getExtension(String pathname){
        String fileName = pathname;
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }

        return extension;
    }

    // edit first line in method to change how filenames are generated
    private File randomizeFilename(File file, String initialextension){
        String filename = "default";
        StringBuilder s = new StringBuilder();
        if(filenamemethod.equals( FilenameMethod.ALPHANUMERIC)){


            int desired_length = 5 + random.nextInt(10);
            for(int x = 0; x<desired_length; x++) {
                int rando_pos = random.nextInt(alphanumeric_chars.length() - 1);
                s.append( alphanumeric_chars.substring(rando_pos, rando_pos+1) );
            }

            filename = s.toString();
        } else{
            s.append(666 + Math.pow((random.nextInt(30)+20), (random.nextInt(3))+2 ));
            s.setLength(s.length()-2);
            filename = s.toString();
        }


        File f = new File(filename+"."+initialextension);
        file.renameTo(f );

        return file;

    }
    private BufferedImage resizeImage(BufferedImage original, int targetW, int targetH) throws IOException{
        BufferedImage resizedImage = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(original, 0, 0, targetW, targetH, null);
        graphics2D.dispose();
        return resizedImage;
    }


    private void configureProgram(){
        String cfgpath = "app.properties";
        Properties cfg = new Properties();
        try {

            cfg.load(new FileInputStream(cfgpath));

            MAX_FILES_TO_READ = Integer.valueOf(cfg.getProperty("MAX_FILES_TO_READ"));
            MINIMUM_RESOLUTION = Integer.valueOf(cfg.getProperty("MINIMUM_RESOLUTION"));
            HEIGHT_LOWER_BOUND = Integer.valueOf(cfg.getProperty("HEIGHT_LOWER_BOUND"));
            HEIGHT_UPPER_BOUND = Integer.valueOf(cfg.getProperty("HEIGHT_UPPER_BOUND"));
            WIDTH_LOWER_BOUND = Integer.valueOf(cfg.getProperty("WIDTH_LOWER_BOUND"));
            WIDTH_UPPER_BOUND = Integer.valueOf(cfg.getProperty("WIDTH_UPPER_BOUND"));
            RESIZE_IMAGES = Boolean.valueOf(cfg.getProperty("RESIZE_IMAGES"));
            RENAME_IMAGES = Boolean.valueOf(cfg.getProperty("RENAME_IMAGES"));
            System.out.println("resize_images equals to "+RESIZE_IMAGES);
            String filenameMethod = cfg.getProperty("FILENAME_METHOD");
            System.out.println("all properties successfully loaded");

            if(filenameMethod.equals("DEFAULT")){
                filenamemethod = FilenameMethod.DEFAULT;
            } else if(filenameMethod.equals("ALPHANUMERIC")){
                filenamemethod = FilenameMethod.ALPHANUMERIC;
            }


        } catch(Exception e) {
            System.out.println("failed to read cfg! using default settings");
        }



    }

    protected static void executeResizeAndRename(Changer c) {
        try{
            c.configureProgram();
            c.readFilePaths();
            c.processImages();
        } catch (Exception e){
            System.out.println("Error");
        }
    }

    /*private static void pullUpGUI(){
        GUIRunner gui = new GUIRunner();
        gui.start();
    }*/

    public static void main(String[] args){
        Changer c = new Changer();
        //pullUpGUI(); -- not implemented
        executeResizeAndRename(c);
    }

}