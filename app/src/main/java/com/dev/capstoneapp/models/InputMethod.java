package com.dev.capstoneapp.models;

public class InputMethod {
   private String label;
   private String key;

   public InputMethod(String label, String key){
      this.label = label;
      this.key = key;
   }

   @Override
   public String toString() {
      return this.label;
   }

   public String getKey() {
      return key;
   }

   public String getLabel() {
      return label;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public void setLabel(String label) {
      this.label = label;
   }
}
