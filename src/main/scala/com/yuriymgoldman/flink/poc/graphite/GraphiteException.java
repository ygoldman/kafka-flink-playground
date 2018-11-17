package com.yuriymgoldman.flink.poc.graphite;

/**
 * Exception that indicates problems when writing to graphite.
 *
 * @author Helmut Zechmann
 *
 */
public class GraphiteException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public GraphiteException(String message) {
    super(message);
  }

  public GraphiteException(String message, Throwable cause) {
    super(message, cause);
  }

}
