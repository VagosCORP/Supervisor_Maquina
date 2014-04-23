/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package supervisor.maquina;

import Bases.cnc;
import java.util.Arrays;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Francisco
 */
public class SeguidorMovimiento extends Service<String>{
    
    cnc equipo=new cnc();
    /**
     * variable dque define el tiempo de espera en milisegundos entre dato y dato solicitado
     */
    public long espera=100;
    
    
    //variable para saber que comando desencadeno el movimiento
    private final StringProperty comando=new SimpleStringProperty();
    
    
    //Propiedades de seguimeitno del estado del equipo
    //ejes ocupados o libres
    public BooleanProperty isCNCruning=new SimpleBooleanProperty();
    //Posicion actual de los ejes, en pulsos
    public IntegerProperty[] pulses=new SimpleIntegerProperty[4];
    //Posicion actual de los ejes, en metros o grados segun aplique
    public DoubleProperty[] pos_r=new SimpleDoubleProperty[4];
    //Posicion comandada de los ejes, en pulsos
    public IntegerProperty[] cmdpulses=new SimpleIntegerProperty[4];
    //Velocidad de los ejes segun la unidad del equipo
    public IntegerProperty[] speed=new SimpleIntegerProperty[4];
    //Velocidad de los ejes en metros/segundo o grados/segundo segun aplique  
    public DoubleProperty[] speed_r=new SimpleDoubleProperty[4];
    //Propiedades para muestra de valores
    public final StringProperty[] showSpeed=new SimpleStringProperty[4];
    public final StringProperty[] showPos=new SimpleStringProperty[4];
    
    private final Double[] rel=new Double[6];
    
    public SeguidorMovimiento(Double r_x,Double r_y,Double r_z,Double r_r,Double r_v,Double r_p){
        rel[0]=r_x;
        rel[1]=r_y;
        rel[2]=r_z;
        rel[3]=r_r;
        rel[4]=r_v;
        rel[5]=r_p;
        for(int c=0;c<4;c++){
            showPos[c]=new SimpleStringProperty("desconocido");
            showSpeed[c]=new SimpleStringProperty("desconocido");
        }

    }
    
    
    public StringProperty getP(int i){
        return showPos[i];
    }
    
    
    public final void setComando(String cmd){
        comando.set(cmd);
    }
    public String getComando(){
        return comando.get();
    }
    private void getPosState(){
        int stat;
        System.out.println("pedire datos!!");
        stat=equipo.get_axis_pos();
        System.out.println("obtuve :"+stat );
        if(stat==0){
            System.out.println("entre al if");
            
            System.out.println("Eje X :"+Integer.toString( equipo.get_c_pos1()));
            System.out.println("Eje Y :"+Integer.toString( equipo.get_c_pos2()));
            System.out.println("Eje Z :"+Integer.toString( equipo.get_c_pos3()));
            System.out.println("Eje R :"+Integer.toString( equipo.get_c_pos4()));
            
            
//            cmdpulses[0].set(equipo.get_c_pos1());
//            cmdpulses[1].set(equipo.get_c_pos2());
//            cmdpulses[2].set(equipo.get_c_pos3());
//            cmdpulses[3].set(equipo.get_c_pos4());
            
            System.out.println("se actualizo c_pos");
                        
//            pulses[0].set(equipo.get_pos1());
//            pulses[1].set(equipo.get_pos2());
//            pulses[2].set(equipo.get_pos3());
//            pulses[3].set(equipo.get_pos4());
            
            System.out.println("se actualizo pos");
            
//            showPos[0].set(Double.toString((double)(equipo.get_pos1()/(rel[5]*rel[0]))));
//            showPos[1].set(Double.toString((double)(equipo.get_pos2()/(rel[5]*rel[1]))));
//            showPos[2].set(Double.toString((double)(equipo.get_pos3()/(rel[5]*rel[2]))));
//            showPos[3].set(Double.toString((double)(equipo.get_pos4()/(rel[5]*rel[3]))));
            
            System.out.println("se actualizo pos mostrada");
            
//            speed[0].set(equipo.get_v1());
//            speed[1].set(equipo.get_v2());
//            speed[2].set(equipo.get_v3());
//            speed[3].set(equipo.get_v4());
            
            System.out.println("se actualizo velocidad en pulsos");
            
//            showSpeed[0].set(Double.toString(speed[0].get()/rel[4]));
//            showSpeed[1].set(Double.toString(speed[1].get()/rel[4]));
//            showSpeed[2].set(Double.toString(speed[2].get()/rel[4]));
//            showSpeed[3].set(Double.toString(speed[3].get()/rel[4]));
            
            System.out.println("se actualizo velocidad mostrada");
           
        }
        System.out.println("sali del if");
    }
    private void getIsRunning(){
        int res =equipo.is_working();
        boolean rng=false;
        if(res!=2){
            if(res==1){
                isCNCruning.set(true);
            }
            else{
                isCNCruning.set(false);
            }
        }
        else{
            //agregar opcion para el perro guardian
        }
    }
  
    
    @Override
    protected Task<String> createTask() {
        return new Task<String>(){

            @Override
            protected String call() throws Exception {
                
                isCNCruning.set(true);
                int c=0;
                System.out.println("iniciando seguidor");
                while(isCNCruning.get()){
                    //agregar un perro guardian para notificar cuando no se obtengan respuestas por un tiempo definido
                    if(c>3){
                        c=0;
                    }
                    System.out.println("no mori 1");
                    getPosState();
                    System.out.println("pedi pos");
                    Thread.sleep(espera);
                    getIsRunning();
                    System.out.println("pedi si esta corriendo");
                    Thread.sleep(espera);
                    c++;
                    
                }
                System.out.println("finalizando seguidor");
                return comando.get();
            }
            
        };
    }
    
}
