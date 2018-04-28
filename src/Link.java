/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author asus
 */
public class Link {
       String link="";
    String text="";
    String title="";
Link(){}
Link(String l,String t,String k){link=l;text=t;title=k;}
void set_link(String l){link=l;}
void set_text(String t){text=t;}
void set_title(String k){title=k;}
String get_link(){return this.link;}
String get_text(){return this.text;}
String get_title(){return title;}
}
