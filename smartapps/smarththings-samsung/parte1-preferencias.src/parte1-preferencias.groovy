/**FALLAN LOS TEXTOS POR DEFCTO QUE NO SE PONEN DIRECTAMENTE EN EL MENSAJE EN EL SIMULADOR
* APLICACIÓN QUE AVISA DE QUE ALGUIEN HA SIDO DETECTADO EN LA LOCALIZACION CUANDO TU LE ESPECIFIQUES LOS PARAMETROS PUDIENDO ALERTAR POR SMS Y/O POR PUSH
* 
*
 *  Parte1_Preferencias
 *
 *  Copyright 2018 ovidio navarro
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Parte1_Preferencias",
    namespace: "SmarthThings_Samsung",
    author: "ovidio navarro",
    description: "Se va a realizar una primera parte para incluirla en una tarea m\u00E1s grande relaccionada con disposiitivos de presencia",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "pagina1", title: "Sensor y mensajes", nextPage: "pag2", install: false, uninstall: true){
	//install en todas las pags a false menos en la ultima
	
        section("Sensor") {
            input ("elSensorPres","capability.presenceSensor",
                    title: "Elegir sensor de presencia?", required: true, multiple:false)
                    //required a true, ya que si no tenemos sensor no funciona la app (es necesario)
                    //multiple false porque solo queremos elegir un sensor de presencia
                    }
        section("Mensajes"){
            input ("textoLlegada", title: "Escribe el Mensaje para cuando alguien llega",
                    defaultValue: "Ha llegado alguien a la localización", 
                    multiple: false, required: false, type: "text")
                    
            input ("textoSalida", title: "Escribe el Mensaje para cuando alguien sale", 
                    defaultValue: "Ya no hay nadie en la localización", 
                    multiple: false, required: false, type: "text") 
       }
             
        section("Cuando avisará "){
            input ("desdeHora", "time", title: "Introduce desde que hora: ")
            input ("hastaHora", "time", title: "Introduce hasta que hora: ")
            mode (name: "modo", title: "Selecciona el Modo en los que avisará: ", 
                    required: true, multiple: true)
            
            }
   	}
	
	 page (name: "pag2", title: "Modo de aviso", uninstall: true)
 		
 }
 def pag2(){ //Definicion de un metodo dinamico que me creará una pag dinamica, que 
 			//se actulizará cada vez que se cambie la entrada opAviso y mostrará o no
            //otras opciones según la seleccion
 	dynamicPage (name: "pag2"){
 		section ("Eleccion de aviso"){
 			input ("opAviso", "enum",title: "Elige la forma del aviso: ",
            		options: ["Solo Push", "Solo SMS", "Push y SMS"], submitOnChange:true)
                    //submitOnChange: cada vez que cambia el campo se recarga la pag
      		}
            if(opAviso=="Solo SMS" || opAviso=="Push y SMS" ||
            	opAviso=="1" || opAviso=="2"){ //VALORES: 0-solo push, 1-solo SMS, 2-Push y SMS
            	section("Seleccionar contactos: "){
                	input ("receptores", "contact", title: "Selecciona los contactos a los que enviar: ")
                    {   	//Si diera error seleccionar desde la agenda, saldría una entrada para escribir el telefono
                    	input ("telefonoEscrito", "phone", title: "Aviso con este texto",
                        		description: "Número Teléfono (usar prefijo +34)", required: false)//input tipo phone
                        } //Cierre input
                   } //cierre seccion contactos
              }//cierre del if
         } //cierre de la pag dinamica
    }// cierre de la variable definida

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
//Suscribirse al nombre (como variable) del sensor, al evento determinado:
	subscribe(elSensorPres, "Presence", presenciaManejador)
    //Al suscribirnos ejecutaremos la clase presenciaManejador. 
   
}
//Manejador del sensor de presencia:
def presenciaManejador(evt){
	/*if (textoLlegada==null){
    	textoLlegada="Ha llegado alguien a la localización";}
    if (textoSalida == null){
    	textoSalida="Ya no hay nadie en la localización";}*/
	log. debug "Manejador de presencia called: $evt"
    //Si detecta presencia: 
	if (evt.value == "present"){
    	log. debug "Presencia detectada"
        //1º Comprobar franja horaria adecuada.
    	if (timeOfDayIsBetween(desdeHora,hastaHora,evt.date, 
    						location.timeZone)){ //evt.date nos indica la hora actual
           log. debug "Zona horaria de aviso"
            //2º comprobar modo en el que estamos: 
           if ( location.mode==modo){
           		log. debug "Modo en el que nos encontramos: $location.mode";
           		// deberia enviar notificacion y/o mensaje segun la opcion seleccionada puesto que se cumplen condiciones
                	if(opAviso=="Solo Push" ||opAviso=="0"){
                        //Enviar notificacion con el texto propuesto: 
                        sendNotification(textoLlegada);  
                        log. debug "Envio push: $textoLlegada"
                    }
                    else if (opAviso=="Solo SMS" ||opAviso=="1"){
                    //comprobar si hay escrito de la agenda: 
                    	if (location.contactBookEnabled && receptores){
                    		sendNotificationToContacts(textoLlegada,recptores)
                            log. debug "Envio sms: $textoLlegada"
                            }
                       	//si no, comprobar que se ha escrito un telefono
                        else if (telefonoEscrito){
                        	sendSms(telefonoEscrito,textoLlegada);
                            log. debug "Envio sms: $textoLlegada"
                            }
                        else{
                        log. debug "No detectado ni agenda ni numero escrito"}
                    }
                    else if (opAviso=="Push y SMS" ||opAviso=="2"){
                    //enviar push
                        sendNotification(textoLlegada);
                        log. debug "Envio push: $textoLlegada"
                    //enviar sms    
                    	if (location.contactBookEnabled && receptores){
                    		sendNotificationToContacts(textoLlegada,recptores)
                            log. debug "Envio sms: $textoLlegada"
                            }
                        else if (telefonoEscrito){
                        	sendSms(telefonoEscrito,textoLlegada);
                            log. debug "Envio sms: $textoLlegada"
                            }   
                   		
                    }
            	} //cierre si esta en modo concreto               
     		}// cierre de franja horaria correcta
    	}//Cierre detectada presencia
        
        
        
        
        
        
    //si no se detecta presencia:     
    else { 
        if(opAviso=="Solo Push" ||opAviso=="0"){
                            //Enviar notificacion con el texto propuesto: 
                            sendNotification(textoSalida);
                            log. debug "Envio push: $textoSalida"
                        }
        else if (opAviso=="Solo SMS" ||opAviso=="1"){
           //comprobar si hay escrito en la agenda: 
           if (location.contactBookEnabled && receptores){
               sendNotificationToContacts(textoSalida,receptores)
               log. debug "Envio sms: $textoSalida"
             }
            //si no, comprobar que se ha escrito un telefono
          	else if (telefonoEscrito){
             sendSms(telefonoEscrito,textoSalida);
             log. debug "Envio sms: $textoSalida"
             }
          else{
            log. debug "No detectado ni agenda ni numero escrito"}
               }
           else if (opAviso=="Push y SMS" ||opAviso=="2"){
              //enviar push
              sendNotification(textoSalida);
              log. debug "Envio push: $textoSalida"
              //enviar sms    
               if (location.contactBookEnabled && receptores){
                     sendNotificationToContacts(textoSalida,receptores)
                     log. debug "Envio sms: $textoSalida"
               }
               else if (telefonoEscrito){
                      sendSms(telefonoEscrito,textoSalida);
                      log. debug "Envio sms: $textoSalida"
           }
        } //final del esse if de push y sms
	} //final else
}// final manejador


// TODO: implement event handlers