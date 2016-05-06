package filerenameftp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FilerenameFTP {

    static String dst = "/Design/Supershift S&L/PRODUCTS/";
    static String excelSource = "G:\\CM\\Category Management Only\\_S0000_Trade marketing\\Pictures Spaceman\\SAP_EAN.xlsx";
    static Map<String, String> myMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        FileInputStream fis_excel = null;
        try {
            fis_excel = new FileInputStream(excelSource);
            XSSFWorkbook wb_excel = new XSSFWorkbook(fis_excel);
            XSSFSheet sheet_excel = wb_excel.getSheetAt(0);
            Iterator rows = sheet_excel.rowIterator();
            while (rows.hasNext()) {
                XSSFRow row = (XSSFRow) rows.next();
                if (row.getCell(1) != null) {
                    myMap.put(row.getCell(0).toString(), row.getCell(1).toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis_excel != null) {
                fis_excel.close();
            }
        }

        FTPClient ftpClient = new FTPClient();
        Utils con = new Utils();
        if (con.connect(ftpClient)) {
            System.out.println("Connected");
            FTPFile[] subDirectories = ftpClient.listDirectories(dst);
            if (subDirectories != null && subDirectories.length > 0) {
                for (int i = 6158; i < subDirectories.length; i += 1) {
                    String currentDirName = subDirectories[i].getName();
                    if (currentDirName.length() == 7) {
                        String sap = currentDirName.substring(0, 2) + "." + currentDirName.substring(2, 5) + "." + currentDirName.substring(5, 7);
                        if (myMap.get(sap) != null) {
                            System.out.println("\n" + i + " " + currentDirName + " (" + sap + ") - " + myMap.get(sap));
                            FTPFile[] subFiles = ftpClient.listFiles(dst + currentDirName);
                            if (subFiles != null && subFiles.length > 0) {
                                for (FTPFile aFile : subFiles) {
                                    StringBuffer currentFileName = new StringBuffer(aFile.getName());
                                    if (currentFileName.toString().equals(".") || currentFileName.toString().equals("..") || currentFileName.toString().equals("Thumbs.db")) {
                                        continue;
                                    }
                                    if (currentFileName.length() > 11) {
                                        String newFilename = currentFileName.insert(currentFileName.indexOf(currentDirName) + 7, "_" + myMap.get(sap).replace("/", "_")).toString();
                                        FileUploadFTP(ftpClient, currentDirName, aFile.getName(), newFilename);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (con.disconnect(ftpClient)) {
                System.out.println("\nDisconnected");
            }
        }
    }

    static void FileUploadFTP(FTPClient ftpClient, String folderName, String oldFileName, String newFileName) throws IOException {

        boolean existed = ftpClient.changeWorkingDirectory(dst + folderName);
        if (existed) {
            boolean rename = ftpClient.rename(oldFileName, newFileName);
            if (rename == true) {
                System.out.println("\t" + oldFileName + " changed into: " + newFileName);
            } else {
                System.out.println("\t" + oldFileName + " not changed !!!");
            }
        } else {
            System.out.println(dst + folderName + " not exists !!!");
        }
    }
}
