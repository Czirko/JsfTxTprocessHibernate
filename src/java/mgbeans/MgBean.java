/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mgbeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.hibernate.Session;
import pojos.Kibocsato;
import pojos.Tipus;

/**
 *
 * @author CzirkO
 */
@ManagedBean
@SessionScoped
public class MgBean {

    private List<Kibocsato> kibocsatok;
    private List<Tipus> tipusok;
    private List<Tipus> szurtTipusok;
    private Kibocsato kivKibocsato;
    private String kivKibNev;
    private Tipus kivTipus;
    private int kivEv;
    private Integer kivKozlonySzam;
    private int kivKibKod;
    private Set<Integer> years;
    private Set<String> kib;

   

    Map<Integer, Kibocsato> kibMap;

    public MgBean() {
        
        Session se = hibernate.HibernateUtil.getSessionFactory().openSession();
        kibocsatok = se.createQuery("FROM Kibocsato").list();
        tipusok = se.createQuery("FROM Tipus").list();
        se.close();
        
        szurtTipusok = new ArrayList<>();
        years = new HashSet<>();
        kib = new HashSet<>();
        
        kibMap = new HashMap<>();
        for (Kibocsato k : kibocsatok) {
            kibMap.put(k.getId(), k);
        }
        for (Tipus t : tipusok) {
            years.add(t.getEv());
            kib.add(kibMap.get(t.getKibocsatoid()).getKozlonynev());

        }
    }

    public void listaTorol() {
        szurtTipusok.clear();
    }

   public void evKivalaszt() {

       for (Tipus t : tipusok) {            //ide logikai feltételnek jönne még a kibocsátó és a kozlonyszám
           if (t.getEv()!=kivEv) {
               tipusok.remove(t);
           }
        

       }}


    public void mappaScan() {
        File root = null;
        try {
            String s = Tipus.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            System.out.println(s);
            root = new File(s+"../../../../KOZLONY"); //megynyitom a kozlony mappát
        } catch (Exception e) {
            System.out.println(e);
        }
        File[] kozlony = root.listFiles();  //tömbbe listázom a tartalmát
        for (File f : kozlony) {

            root = f; //végigmegy a kozlony alatti mappákban
            String[] db = f.getName().split("_");
            String azonosito = db[0];
            String kibocsalto = db[1];
            System.out.println(kibocsalto);

            File[] evszam = root.listFiles();

            for (File h : evszam) {
                int year = Integer.parseInt(h.getName());
                if (year > 1979) {
                    System.out.println(year);
                    root = h; //évszámoknál járunk
                    File[] txts = root.listFiles();
                    for (File txt : txts) {
                        if (Character.isDigit(txt.getName().charAt(0))) {
                            Kibocsato k = new Kibocsato();
                            k.setAzonosito(Integer.parseInt(azonosito));
                            k.setKozlonynev(kibocsalto);
                            kibocsatok.add(k);
                            
                            Session se = hibernate.HibernateUtil.getSessionFactory().openSession();
                            se.beginTransaction();
                            se.saveOrUpdate(k);
                            se.getTransaction().commit();
                            se.close();
                            
                            int kozlonySzam = Integer.parseInt(txt.getName().split("\\.")[0]);
                            txtFeldolgozas(txt, k, kozlonySzam, year);

                        }

                    }
                }

            }

        }
    }

    public void txtFeldolgozas(File file, Kibocsato k, int kozlonySzam, int ev) {

        try {

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            int index = 0;
            String[] adatnevek = null;
            String sor = br.readLine();
            while (sor != null) {
                if (index == 0) {
                    adatnevek = sor.split("\t");

                } else if (index > 1) {
                    String[] s = sor.split("\t");
                    Tipus t = new Tipus();
                    String data = "";
                    for (int i = 0; i < s.length; i++) {
                        if(i>=s.length-5){
                            if(s[i].contains(".")){
                                s[i]="true";
                            }else{
                                s[i]="false";
                            }
                        }
                        data += adatnevek[i] + ": " + s[i] + "\n";
                        
                    }
                    t.setEv(ev);
                    t.setKozlonyszam(kozlonySzam);
                    t.setKibocsatoid(k.getId());
                    t.setJogszabalyadat(data);

                    tipusok.add(t);

                    Session se = hibernate.HibernateUtil.getSessionFactory().openSession();
                    se.beginTransaction();
                    se.saveOrUpdate(t);
                    se.getTransaction().commit();
//                   kibocsatok = se.createQuery("FROM Kibocsato").list();
//                   tipusok = se.createQuery("FROM Tipus").list();
                    se.close();

                }
                sor = br.readLine();

                index++;
            }

            br.close();

        } catch (FileNotFoundException ex) {
            System.out.println("Probléma a " + file.getName() + " nevü file megnyitása közben. " + ex);
        } catch (IOException ex) {
            System.out.println("IO hiba a " + file.getName() + " feldolgozásása során");
        }
    }

    public List<Kibocsato> getKibocsatok() {
        return kibocsatok;
    }

    public void setKibocsatok(List<Kibocsato> kibocsatok) {
        this.kibocsatok = kibocsatok;
    }

    public List<Tipus> getTipusok() {
        return tipusok;
    }

    public void setTipusok(List<Tipus> tipusok) {
        this.tipusok = tipusok;
    }

    public Kibocsato getKivKibocsato() {
        return kivKibocsato;
    }

    public void setKivKibocsato(Kibocsato kivKibocsato) {
        this.kivKibocsato = kivKibocsato;
    }

    public Tipus getKivTipus() {
        return kivTipus;
    }

    public void setKivTipus(Tipus kivTipus) {
        this.kivTipus = kivTipus;
    }

    public int getKivEv() {
        return kivEv;
    }

    public void setKivEv(int kivEv) {
        this.kivEv = kivEv;
    }

    public Integer getKivKozlonySzam() {
        return kivKozlonySzam;
    }

    public void setKivKozlonySzam(Integer kivKozlonySzam) {
        this.kivKozlonySzam = kivKozlonySzam;
    }

    

    public int getKivKibKod() {
        return kivKibKod;
    }

    public void setKivKibKod(int kivKibKod) {
        this.kivKibKod = kivKibKod;
    }

    public Map<Integer, Kibocsato> getKibMap() {
        return kibMap;
    }

    public void setKibMap(Map<Integer, Kibocsato> kibMap) {
        this.kibMap = kibMap;
    }

    public String getKivKibNev() {
        return kivKibNev;
    }

    public void setKivKibNev(String kivKibNev) {
        this.kivKibNev = kivKibNev;
    }
    public List<Tipus> getSzurtTipusok() {
        return szurtTipusok;
    }

    public void setSzurtTipusok(List<Tipus> szurtTipusok) {
        this.szurtTipusok = szurtTipusok;
    }
    
     public Set<String> getKib() {
        return kib;
    }

    public void setKib(Set<String> kib) {
        this.kib = kib;
    }

    public Set<Integer> getYears() {
        return years;
    }

    public void setYears(Set<Integer> years) {
        this.years = years;
    }

    

}
