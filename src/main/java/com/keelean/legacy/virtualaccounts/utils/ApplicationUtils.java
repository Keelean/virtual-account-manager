package com.keelean.legacy.customeraccounts.utils;

import com.google.gson.Gson;


public class ApplicationUtils {

    /**
   * Object to json.
   *
   * @param object the object
   * @return the string
   */
  public static String objectToJson(Object object) {
    Gson gson = new Gson();
    return gson.toJson(object);
  }

}
