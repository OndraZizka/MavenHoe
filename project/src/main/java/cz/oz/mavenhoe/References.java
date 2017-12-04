package cz.oz.mavenhoe;

import cz.oz.mavenhoe.mappers.FileBasedMapper;

/**
 * This is a dumpground where I put references which I am lazy to properly store somewhere :)
 * @author Ondrej Zizka
 */
public class References {

    private FileBasedMapper fileBasedMapper;

    public FileBasedMapper getFileBasedMapper() {
      return fileBasedMapper;
    }

    public void setFileBasedMapper(FileBasedMapper fileBasedMapper) {
      this.fileBasedMapper = fileBasedMapper;
   }

}