/*  This code is released into the public domain, so use it, hack it and
    package it as you wish.  */

import java.io.Reader;
import java.io.FilterReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.IOException;

public class JSONFilterReader extends FilterReader {
  private static final int DEFAULT = 0, STRING = 1, STRING_ESCAPE = 2;
  private int state = 0;  
  
  public JSONFilterReader(Reader inrd) {
    super(inrd);
    if (!in.markSupported()) {
      throw new UnsupportedOperationException("JSONFilterReader cannot work with a Reader that doesn't support mark()");
    }
  }
  
  public int read() throws IOException {
    int c = in.read();
    int d = 0, result = 0;
    
    switch (state) {
      case STRING_ESCAPE:
        if (c == '"') {
          state = 1;
        }
        result = c;
      break;
      case STRING:
        if (c == '"') {
          state = 0;
          result = c;
        } else if (c == '\\') {
          state = 2;
        } else {
          result = c;
        }
      break;
      default:
        if (c == '"') {
          state = 1;
          result = c;
        } else if (c == '/') {
          in.mark(1);
          d = in.read();
          if (d == '*') {
            //  Hit a block comment, find the end of it and jump there.
            while (true) {
              c = in.read();
              if (c == '*') {
                in.mark(1);
                d = in.read();
                if (d == '/' || d == -1) {
                  break;
                } else {
                  in.reset();
                }
              } else if (c == -1) {
                break;
              }
            }
            
            result = in.read();
          } else if (d == '/') {
            //  Hit a line comment, do as above.
            while (true) {
              c = in.read();
              if (c == '\n' || c == '\r' || c == -1) {
                break;
              }
            }
            
            result = c;
          } else {
            in.reset();
            result = c;
          }
        } else {
          result = c;
        }
       break;
    }
    
    return result;
  }
  
  public int read(char[] cbuf, int offset, int len) throws IOException {
    int c, i;
    for (i = 0; i < len; i++) {
      c = read();
      if (c == -1) {i = c; break;}
      cbuf[offset + i] = (char) c;
    }
    
    return i;
  }
  
  public long skip() throws IOException {
    throw new UnsupportedOperationException("this class does not support the skipping of characters");
  }
  
  public static void main(String[] args) throws IOException {
    JSONFilterReader rdr = new JSONFilterReader(new BufferedReader(new InputStreamReader(System.in)));
    int i;
    while (true) {
      i = rdr.read();
      if (i == -1) {break;}
      System.out.print((char) i);
    }
  }
}