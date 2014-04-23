/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package supervisor.maquina;

import Bases.TabletCMD;
import Bases.cnc;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;


/**
 *
 * @author Francisco
 */
public class ControlMaquinaController implements Initializable {
   Double relx=30.0;
   Double rely=40.0;
   Double relz=40.0;
   Double relr=40.0/360;
   Double relv=166.666667;
   Double relp=10000.0;
   
   Double temperaturas[]=new Double[4];
   int nMuestras=20;
   //variables para muestra de temperaturas
    CategoryAxis ejeX=new CategoryAxis();
    NumberAxis ejeY=new NumberAxis("Temperatura", 0, 40, 1);
    public static XYChart.Series sensor1=new XYChart.Series();
    public static XYChart.Series sensor2=new XYChart.Series();
    public static XYChart.Series sensor3=new XYChart.Series();
    public static XYChart.Series sensor4=new XYChart.Series();
   //manejo de datos
    @FXML
    BarChart<String,Number> bcjavalinas=new BarChart<>(ejeX,ejeY);
   //coso sebas
   TabletCMD tabletcmd;
   //variables de control
   Boolean ctablet=false;
   GruaCnc grua=new GruaCnc(
           (double)0,//x_0
           (double)0,//y_0
           (double)0,//z_0
           (double)1,//longitud del agitador
           relx,//reduccion x
           rely,
           relz,
           relr,
           relp,
           relv);
   
   
   cnc controlador=new cnc();
   ////////////////////
   
   TabletCMD remoto;
   

   @FXML
   private Label lblConeccion;
   
//labels de muestra de datos de posición  
    @FXML
    private Label PX;
    @FXML
    private Label PY;
    @FXML
    private Label PZ;
    @FXML
    private Label PR;
    
    
////cuadros de texto para movimiento
   @FXML 
   private TextField txtFldPosX;
   @FXML
   private TextField txtFldPosY;
   @FXML
   private TextField txtFldPosZ;
   @FXML
   private TextField txtFldPosR;
    
//cuadros de texto para configuración
    
    
    @FXML
    private TextField txtFldVi;
    @FXML
    private TextField txtFldVm;
    @FXML
    private TextField txtFldAcc;
    @FXML
    private TextField txtFldPd;
    @FXML
    private TextField txtFldPuerto;
    @FXML
    private TextField txtFldIP;
    
    @FXML
    private ChoiceBox<Short> chBoxEjes;
    
    //botones
    @FXML
    private Button btnConfigurar;        
 
    
//eventos de botones de movimiento
    @FXML
    private void handleButtonMover(ActionEvent e){
        grua.mover(Double.parseDouble(txtFldPosX.getText()),
                Double.parseDouble(txtFldPosY.getText()),
                Double.parseDouble(txtFldPosZ.getText()),
                Double.parseDouble(txtFldPosR.getText()));
    }
    
//eventos de botones de movimiento
    Temperaturas adqTemp=new Temperaturas();
    Double coso=0.000;
    
    @FXML
    private void handleButtonAdquirirTemp(ActionEvent e){
        adqTemp.iniciarMuestreo(nMuestras);
        adqTemp.start();
    }    
    
    

//eventos de botones de configruacion    
    @FXML
    private void handleButtonConnect(ActionEvent e){
        grua.conectar_asinc(txtFldIP.getText(), Integer.parseInt(txtFldPuerto.getText()));
    }
    @FXML
    private void handleButtonConfigAll(ActionEvent e){
        short[] axis=new short[]{1,2,3,4,5,6};
        int isp=(int)(Integer.parseInt(txtFldVi.getText())*166.6667);
        int sp=(int)(Integer.parseInt(txtFldVm.getText())*166.6667); 
        int aacc=(int)(Integer.parseInt(txtFldAcc.getText())*166.6667); 
        int[] init_speed=new int[]{isp,
            isp,
            isp,
            isp,
            isp,
        isp};
        int[] speed=new int[]{sp,
            sp,
            sp,
            sp,
            sp,
        sp};
        int[] acc=new int[]{aacc,
            aacc,
            aacc,
            aacc,
            aacc,
        aacc};
        int[] pmm=new int[]{Integer.parseInt(txtFldPd.getText()),
            Integer.parseInt(txtFldPd.getText()),
            Integer.parseInt(txtFldPd.getText()),
            Integer.parseInt(txtFldPd.getText()),
            Integer.parseInt(txtFldPd.getText()),
        Integer.parseInt(txtFldPd.getText())};
        short num=6;
        
        controlador.mov_parameter(axis, init_speed, speed, acc, pmm, num);
    }
   
     @FXML 
     void handleButtonDetener(ActionEvent e){
         ctablet=false;
         controlador.stop();
         tabletcmd.comunic.Detener_Actividad();
     }
     
     
     
     
     
     
     
     
     
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        tabletcmd=new TabletCMD(2000);


        
        bcjavalinas.getXAxis().setLabel("Sensores");
        bcjavalinas.getYAxis().setLabel("Temperatura [°C]");

        bcjavalinas.getYAxis().autoRangingProperty().set(false);
        bcjavalinas.setTitle("SENSORES");

        
        
        
        //sensor1.getData().add(new XYChart.Data(javalina1,0));
        
        temperaturas[0]=0.00;
        temperaturas[1]=0.00;
        temperaturas[2]=0.00;
        temperaturas[3]=0.00;
        
        sensor1.setName("Sensor-1");
        sensor1.getData().add(new XYChart.Data("Sensor-1",temperaturas[0]=0.00));
        sensor2.setName("Sensor-2");
        sensor2.getData().add(new XYChart.Data("Sensor-2",temperaturas[0]=0.00));
        sensor3.setName("Sensor-3");
        sensor3.getData().add(new XYChart.Data("Sensor-3",temperaturas[0]=0.00));
        sensor4.setName("Sensor-4");
        sensor4.getData().add(new XYChart.Data("Sensor-4",temperaturas[0]=0.00));

        bcjavalinas.getData().addAll(sensor1,sensor2,sensor3,sensor4);
        bcjavalinas.setAnimated(false);        
        

        //Adquisición de temperaturas finalizada
        adqTemp.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                temperaturas=adqTemp.getValue();
                adqTemp.reset();
                adqTemp.iniciarMuestreo(nMuestras);
                adqTemp.start();
                int c=0; 
                for (XYChart.Series<String, Number> series : bcjavalinas.getData()) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        data.setYValue(temperaturas[c]);
                        c++;
                    }
            
                }
            }
        });
        adqTemp.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println( "Error en el hilo reiniciando hilo");
                adqTemp.reset();
                adqTemp.iniciarMuestreo(nMuestras);
                adqTemp.start();
            }
        });
        adqTemp.setOnCancelled(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println( "Timeout excedido reiniciando hilo");
                adqTemp.reset();
                adqTemp.iniciarMuestreo(nMuestras);
                adqTemp.start();
            }
        });
        
        
        tabletcmd.asignarListener(new TabletCMD.OrdenRecibida() {

            @Override
            public void conectado() {
                
            }

            @Override
            public void mover_a(float x, float y, float z, float a) {
                grua.mover((double)y/1000,(double)x/1000,(double)z/1000,(double)(-a));
            }

            @Override
            public void desconectado() {
                
            }
        });
        
        PX.textProperty().bind(grua.show_posPortico);
        PY.textProperty().bind(grua.show_posCarro);
        PZ.textProperty().bind(grua.show_posElevador);
        PR.textProperty().bind(grua.show_posRotador);
        
        txtFldAcc.setText("300");
        txtFldPd.setText("100");
        txtFldVi.setText("500");
        txtFldVm.setText("1700");
        
        txtFldIP.setText("20.0.0.88");
        txtFldPuerto.setText("502");
        
        txtFldPosX.setText("0");
        txtFldPosY.setText("0");
        txtFldPosZ.setText("0");
        txtFldPosR.setText("0");
        
        grua.setOnConnectionListener(new GruaCnc.OnConnectionListener() {

            @Override
            public void OnConnectionSucces() {
                lblConeccion.setText("Conectado");
            }

            @Override
            public void OnConnectionExistsErr() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnConnectionMaxLimitErr() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnConnectionSocketErr() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnConnectionTcpErr() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        
        
        grua.setOnOnMovementListener(new GruaCnc.OnMovementListener() {

            @Override
            public void OnCommandFailed(String fuente) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnMovementSucces() {
                System.out.println("Movimiento Finalizado");
                tabletcmd.comunic.Detener_Actividad();
            }

            @Override
            public void OnGoToWagonSucces() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnMixingSucces() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnMixingStepSucces() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnMixingAproachSucces() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnMixerRetractSucces() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnGoToSamplingPointSucces() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void OnPositionUpdate(Double x, Double y, Double z, Double r) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                System.out.println("Posicion actualizada");
                if(tabletcmd.comunic.estado==tabletcmd.comunic.CONNECTED){
                    //actualizar informacion en la tablet
                    tabletcmd.comunic.enviar("X="+Double.toString(Math.abs(grua.posCarro*1000))+
                            "=Y="+Double.toString(Math.abs(grua.posPortico*1000))+
                            "=Z="+Double.toString(Math.abs(grua.posElevador*1000))+
                            "=A="+Double.toString(Math.abs(grua.posRotador))+"=/");
                    //ver que se esta enciando a la tablet
                    System.out.println("X="+Double.toString(Math.abs(grua.posCarro*1000))+
                            "=Y="+Double.toString(Math.abs(grua.posPortico*1000))+
                            "=Z="+Double.toString(Math.abs(grua.posElevador*1000))+
                            "=A="+Double.toString(Math.abs(grua.posRotador))+"=/");
                }
                
            }

            @Override
            public void OnDeviveIsBussy() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }    
    
}
