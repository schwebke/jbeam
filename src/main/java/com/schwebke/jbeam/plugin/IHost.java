package com.schwebke.jbeam.plugin;

public interface IHost
{
   boolean lockUI();
   void unlockUI();
   void resetApp();
   IModel getModel();
   double getDefEI();
   double getDefEA();
   void modelHasChanged();
}
