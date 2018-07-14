/**
 *  4_ControlDePuertasVentanas, PROBAR CON MULTIPLES PUERTAS.... (USO DE LISTAS)
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
    name: "4_ControlDePuertasVentanas",
    namespace: "SmarthThings_Samsung",
    author: "ovidio navarro",
    description: "aplicaci\u00F3n para que si una puerta o una ventana se abren y no se cierran despu\u00E9s de un n\u00FAmero determinado de minutos se mande una notificaci\u00F3n indicando que la puerta se ha quedado abierta. Los minutos los indica el usuario en las preferencias. Si el usuario tiene agenda de contacto y especifica un contacto se env\u00EDa un SMS. En otro caso la notificaci\u00F3n es de tipo \u201CPush\u201D.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page (name:"pag1", title: "Modo de aviso", uninstall: false)
    }
def pag1(){
	dynamicPage (name: "pag1"){
        section("Información sobre dispositivos") {
            input("sensorPuerta", "capability.contactSensor", 
                title:"Que sensores controlar", required:true, multiple: false)
            input ("tiempoAvisar", "number", title: "Tras cuantos segundos avisar?")
            input ("textoAvisoPuerta", title: "Escribe el Mensaje de aviso", 
                        defaultValue: "Se ha dejado la puerta abierta", 
                        multiple: false, required: false, type: "text") 

            input ("opAviso", "enum",title: "Elige la forma del aviso: ",
                    options: ["Solo Push", "Solo SMS", "Push y SMS"], submitOnChange:true)
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
    
	}
}


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
subscribe(sensorPuerta, "contact", contactoManejador)
	// TODO: subscribe to attributes, devices, locations, etc.
}

def contactoManejador (evt){
//Si detecta contacto abierto: debe esperar x segundos y avisar
    if (evt.value =="open"){
    log.debug "Puerta abierta, esperar los segundos definidos y comprobar si avisar";
	runIn(tiempoAvisar, apagarTras);
        }
    else{
    log.debug "Puerta cerrada";
    }
}

def apagarTras(){
    //con el metodo evaluamos la ultima vez que cambio el atributo
	def sensor_ultimo_cambio = sensorPuerta.latestState("contact"); 
    def sensor_actual = sensorPuerta.currentState("contact"); //estado actual
    
	if(sensor_actual.value=="open"){
    	    log.debug "esta inactivo, ver si avisar.."

		if((now()- sensor_ultimo_cambio.date.time)>tiempoAvisar){
        	log.debug "tras la espera, avisa"
            avisarPuertaAbierta();
            }
        else{
        	 log.debug "Hace menos del tiempo estuvo abierta, no avisamos";
             runIn(tiempoAvisar, apagarTras);
             }
        }
      else{
      log.debug "Actualmente esta cerrada";
      	}
}

def avisarPuertaAbierta(){
	if(opAviso=="Solo Push" ||opAviso=="0"){
        //Enviar notificacion con el texto propuesto: 
        sendNotification(textoAvisoPuerta);  
        log. debug "Envio push: $textoAvisoPuerta"
    }
    else if (opAviso=="Solo SMS" ||opAviso=="1"){
        //comprobar si hay escrito de la agenda: 
        if (location.contactBookEnabled && receptores){
            sendNotificationToContacts(textoAvisoPuerta,recptores)
            log. debug "Envio sms: $textoLlegada"
        }
        //si no, comprobar que se ha escrito un telefono
        else if (telefonoEscrito){
            sendSms(telefonoEscrito,textoAvisoPuerta);
            log. debug "Envio sms: $textoLlegada"
        }
        else{
            log. debug "No detectado ni agenda ni numero escrito"}
    }
    else if (opAviso=="Push y SMS" ||opAviso=="2"){
        //enviar push
        sendNotification(textoAvisoPuerta);
        log. debug "Envio push: $textoAvisoPuerta"
        //enviar sms    
        if (location.contactBookEnabled && receptores){
            sendNotificationToContacts(textoAvisoPuerta,recptores)
            log. debug "Envio sms: $textoAvisoPuerta"
        }
        else if (telefonoEscrito){
            sendSms(telefonoEscrito,textoAvisoPuerta);
            log. debug "Envio sms: $textoAvisoPuerta"
        }   
                   		
    }
}