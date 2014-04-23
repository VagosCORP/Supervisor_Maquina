/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package supervisor.maquina;

import Bases.cnc;
import java.text.DecimalFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 *
 * @author Francisco
 */
public class Supervisor {
    
    DecimalFormat df=new DecimalFormat("0.00");
    
    cnc equipo=new cnc();
    /**
     * variable dque define el tiempo de espera en milisegundos entre dato y dato solicitado
     */
    public long espera=100;
    
    //Estado del equipo
    public Boolean ocupado;
    //variables de posición en metros
    public Double posX; //metros
    public Double posY; //metros
    public Double posZ; //metros
    public Double posR; //metros
    
    public Double velX; //rpm
    public Double velY; //rpm
    public Double velZ; //rpm
    public Double velR; //rpm
    
    public StringProperty show_vX=new SimpleStringProperty("Desconocido");
    public StringProperty show_vY=new SimpleStringProperty("Desconocido");
    //agregar si es necesario las properties del tema de movimiento
    
    public StringProperty show_pX=new SimpleStringProperty("Desconocido");
    public StringProperty show_pY=new SimpleStringProperty("Desconocido");
    public StringProperty show_pZ=new SimpleStringProperty("Desconocido");
    public StringProperty show_pR=new SimpleStringProperty("Desconocido");
    //variables de posicion en pulsos
    public Integer pX;
    public Integer pY;
    public Integer pZ;
    public Integer pR;
    
    public Integer vX;
    public Integer vY;
    public Integer vZ;
    public Integer vR;
    
    
    public Integer p1_old=0;
    public Integer p2_old=0;
    
    public Integer delta1;
    public Integer delta2;
    //constantes dimensionales del equipo
    public final Double x0; //distancia entre el punto de origen y el eje de inclinacion del cabezal en eje x
    public final Double y0; //distancia entre el punto de origen y el eje de inclinacion del cabezal en eje y
    //Relaciones de movimiento
    public final Double Rx; //Relacion revoluciones motor vs desplazamiento en metros metros en x
    public final Double Ry; //Relacion revoluciones motor vs desplazamiento en metros metros en y
    public final Double Rz; //Relacion revoluciones motor vs desplazamiento en metros metros en x
    public final Double Rr; //Relacion revoluciones motor vs desplazamiento en metros metros en y
    public final Double Rv; //Relacion entre velocidad en pulsos y rpm
    
    private final ObData servicio=new ObData();
    
    
    OnMovementListener movListener;
    public interface OnMovementListener{
        
        public void OnMovementSucces();//movimiento libre en XY finalizado
        public void OnMovXfail();//descordinacion entre los dos motores del eje X
        public void OnUpdate(Double x,Double y,Double z,Double r);
    }
    public void setOnOnMovementListener(OnMovementListener mvLst){
        movListener=mvLst;
    }
    
    
    public Supervisor(double x_0,double y_0,double r_x,double r_y,double r_v,double r_z,double r_r){

        x0=x_0;
        y0=y_0;
        Rx=r_x;
        Ry=r_y;
        Rz=r_z;
        Rr=r_r;
        Rv=r_v;
        servicio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println("Adquissicion finalizada!");
                pX=servicio.pulsos1;
                pY=servicio.pulsos3;
                pZ=servicio.pulsos4;
                pR=servicio.pulsos5;
                
                System.out.println("Eje X : "+pX);
                System.out.println("Eje Y : "+pY);
                System.out.println("Eje Z : "+pZ);
                System.out.println("Eje R : "+pR);
                
                vX=servicio.velocidad1;
                vY=servicio.velocidad3;
                vZ=servicio.velocidad4;
                vR=servicio.velocidad5;
                
                velX=vX/Rv;
                velY=vY/Rv;
                //ver luego si se puede impliementar el tema velocidad
                
                show_vX.set(velX.toString());
                show_vY.set(velY.toString());
                
                
                System.out.println("Velocidad X : "+vX);
                System.out.println("Velocidad Y : "+vY);
                
                posX=pX.doubleValue()/(10000.00*Rx);
                posY=pY.doubleValue()/(10000.00*Ry);
                posZ=pZ.doubleValue()/(10000.00*Rz);
                posR=pR.doubleValue()/(10000.00*Rr);
                
                movListener.OnUpdate(posX*1000, posY*1000, posZ*1000, posR*1000);
                
                System.out.println("Pos  X: "+pX);
                System.out.println("Pos  Y: "+pY);
                System.out.println("Pos  Z: "+pZ);
                System.out.println("Pos  R: "+pR);
                
                
                
                
                show_pX.set(df.format(posX));
                show_pY.set(df.format(posY));
                show_pZ.set(df.format(posZ));
                show_pR.set(df.format(posR));
                
                delta1=Math.abs(servicio.pulsos1-p1_old);
                delta2=Math.abs(servicio.pulsos2-p2_old);
                System.out.println("delta 1 :"+delta1);
                System.out.println("delta 2 :"+delta2);
                p1_old=servicio.pulsos1;
                p2_old=servicio.pulsos2;
                if((delta1>=(1.2*delta2))||((1.2*delta1)<=delta2)){
                    movListener.OnMovXfail();
                    System.out.println("disque fallo, ajustar en base a los resultados reales");
                    //equipo.stop();
                }
                ocupado=servicio.state;
                System.out.println("Estado :"+ocupado);
                if(!ocupado){
                    movListener.OnMovementSucces();
                }
                servicio.reset();
            }
        });
    }
    public void actualizarPos(String com){
        servicio.setComando("a modificar");
        if(servicio.getState()==Service.State.READY){
            servicio.start();
        }
        else if(servicio.isRunning()){
            System.out.println("Adquiriendo datos");
        }
        else{
            System.out.println("Servicio :"+servicio.getState()+"reseteando e iniciando");
            servicio.reset();
            servicio.start();
        }
    }
    
    private static class ObData extends Service<String>{
        
        cnc grua = new cnc();
        
        boolean state;
        public Integer pulsos1=0;
        public Integer pulsos2=0;
        public Integer pulsos3=0;
        public Integer pulsos4=0;
        public Integer pulsos5=0;
                
        public Integer velocidad1=0;
        public Integer velocidad2=0;
        public Integer velocidad3=0;
        public Integer velocidad4=0;
        public Integer velocidad5=0;
        
        private String comando;
        public void setComando(String cmd){
            comando=cmd;
        }
        @Override
        protected Task<String> createTask() {
            return new Task<String>(){
                @Override
                protected String call() throws Exception {
                    
                    if(grua.get_axis_pos()==0){
                        pulsos1=grua.get_pos1();
                        System.out.println("Pos1= "+pulsos1);
                        pulsos2=grua.get_pos2();
                        System.out.println("Pos2= "+pulsos2);
                        pulsos3=grua.get_pos3();
                        System.out.println("Pos3= "+pulsos3);
                        pulsos4=grua.get_pos4();
                        pulsos5=grua.get_pos5();
                        
                        velocidad1=grua.get_v1();
                        System.out.println("V1= "+velocidad1);
                        velocidad2=grua.get_v2();
                        System.out.println("V2= "+velocidad2);
                        velocidad3=grua.get_v3();
                        System.out.println("V3= "+velocidad3);
                        velocidad4=grua.get_v4();
                        velocidad5=grua.get_v5();
                        
                        
                    }
                    else{
                        System.out.println("Fallo al actualizar las variables");
                    }
                    int res=grua.is_working();
                    System.out.println("Estado del equipo : "+res);
                    if(res!=2){
                        if(res==0){
                            state=false;
                        }
                        if(res==1){
                            state=true;
                        }
                    }
                    else{
                        System.out.println("Fallo al obtener estado del equipo");
                    }
                    
                    return comando;
                }
                
            };
        }
        
    }
    
    
    
}
