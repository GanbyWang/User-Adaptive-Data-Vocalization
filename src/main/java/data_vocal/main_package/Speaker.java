package data_vocal.main_package;

import java.io.InputStream;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Class for speaking result
 * Uses Watson SDK
 * */
public class Speaker {
	
	/**
	 * Function to read out the final result
	 * The audio won't get stored
	 * @param: the string type speech
	 * */
	public void speak(String speech) {
		TextToSpeech service = new TextToSpeech();
	    	service.setUsernameAndPassword(WatsonAccount.accountName, WatsonAccount.accountPwd);
	
	    	try {
	    		  InputStream stream = service.synthesize(speech, Voice.EN_ALLISON,
	    		    AudioFormat.WAV).execute();
	    		  
	    		  // Add the file head
	    		  InputStream in = WaveUtils.reWriteWaveHeader(stream);
	    		  
	    		  // Read out the result
	    		  AudioStream audioStream = new AudioStream(in);
	    		  AudioPlayer.player.start(audioStream);
	    		  
	    		  in.close();
	    		  stream.close();
	    		  
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	}
}
