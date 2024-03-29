package businesscard;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.JTextField;
public class RFID {
	private String PORT_NAME;
	private static final int TIME_OUT = 2000;
	private int DATA_RATE;
	private SerialPort serialPort;
	Payment pay;
public RFID(Payment pay){
	this.pay = pay;
}
public void setPort(String PORT_NAME){
	this.PORT_NAME = PORT_NAME;
}
public void setRate(int DATA_RATE){
	this.DATA_RATE = DATA_RATE;
}
public void initialize(){
	CommPortIdentifier portId = null;
	try{
		portId = CommPortIdentifier.getPortIdentifier(PORT_NAME);
	}catch (NoSuchPortException e){
		e.printStackTrace();
	}
	try{
		this.serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
		this.serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
		SerialPort.PARITY_NONE);
		this.serialPort.addEventListener(new MyListener(this.serialPort,pay));
		this.serialPort.notifyOnDataAvailable(true);
	}catch (Exception e){
		e.printStackTrace();
	}
}

public synchronized void close(){
	
	if (this.serialPort != null) {
		this.serialPort.removeEventListener();
		this.serialPort.close();
	}
	
}
}
class MyListener implements SerialPortEventListener{
	private final SerialPort port;
	Payment pay;
	StringBuilder builder = new StringBuilder();
public MyListener(SerialPort port,Payment pay){
	super();
	this.port = port;
	this.pay = pay;
}
public void serialEvent(SerialPortEvent event){
	if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE){
		try{
			int available = this.port.getInputStream().available();
			byte chunk[] = new byte[available];
			this.port.getInputStream().read(chunk, 0, available);
			String input=new String(chunk);
			builder.append(input);
			writeLog();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
public void writeLog(){
	try{
		System.out.println(builder.toString());
		if(builder.length() == 12){
			pay.tf2.setText(builder.toString());
			String sid = pay.tf1.getText().toString().trim();
			String card = pay.tf2.getText().toString().trim();
			String amount = pay.tf3.getText().toString().trim();
			String msg = DBCon.deductAmount(sid,Double.parseDouble(amount),card);
			builder.delete(0,builder.length());
			if(msg.equals("success"))
				pay.l3.setText("Transaction Completed");
			else
				pay.l3.setText(msg);
			pay.clear();
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}
}
