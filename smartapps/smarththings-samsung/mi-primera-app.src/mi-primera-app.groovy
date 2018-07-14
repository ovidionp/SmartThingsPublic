/** 
 *  Mi primera app
 * 	Enciende un led cuando detecta presencia. Y lo apaga cuando no la detecta
 *	EXTRA: Se le ha añadido una espera tras no detectar movimiento y tras esto 
 * 			comprueba si hay movimiento o no y si sigue sin haber se apaga.
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
    name: "Mi primera app",
    namespace: "SmarthThings_Samsung",
    author: "ovidio navarro",
    description: "Mi primera aplicaci\u00F3n realizado en el curso IoT",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Selecciona el sensor de movimiento") {
    
    	input ("tiempoApagar", "number",title:"timpo apagado",  ) //ESTE ES EL ERROR EL TIPO QUE NO FUNCIONA CON RUNIN
        
    // el nombre y la capacidad debe de estar en ese orden, los demas parametro es igual el orden
    	input "elSensorMov","capability.motionSensor",
 		title: "Sensor Movimiento?", required: true, multiple: false
	}
   section("Selecciona la luz a encender/apagar:") {
			input "elInterruptor","capability.switch",
 			title: "Interruptor?", required: true, multiple: false
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
// initialize se puede llamar como sea. 
def initialize() {
//me suscribo a el nombre(como variable), a el evento motion.active (para cada atributo un evento). Y esto ejecutará el manejador de eventos (movimientoDetectadoManejador)
		subscribe(elSensorMov, "motion", movimientoDetectadoManejador)
        /*ya no hace falta una segunda suscripcion si lo suscribimos al objeto completamente ya que desde ahi vemos todos sus parámetros 
        subscribe(elSensorMov,"motion.inactive",movimientoDetectadoManejador)*/

	// TODO: subscribe to attributes, devices, locations, etc.
}


// def - es para interpretar lo que devuelve (es decir no se debe especificar el valor que devulve), 
//metodo obligatioriamente debe tener un parámetro que sea event(que no es necesario concretar, ya que gruby lo adivinará)
def movimientoDetectadoManejador (evt){
//llamamos a la variable que hemos definido para el switch con su atributo on
 log.debug "movimientoDetectadoManejador called: $evt" //con el dollar contenido de la variable.(Gruby)
 if (evt.value =="active"){
 	log.debug "movimiento detectado-enciende interruptor";
    elInterruptor.on()
    }
 //si no se detecta movimiento:
 else if (evt.value=="inactive"){
 	/*elInterruptor.off()//-Cambiamos apagarlo directamente por apagarlo tras unos segundos*/
    log.debug "movimiento desactivado-espera ${tiempoApagar} segundos interruptor";
    runIn(tiempoApagar, apagarTras); //Dormirse esos segundos y abrir el manejador
    }
 /*EXTRA, no hay movimiento y me duermo x segundos y al volver veo si no ha habido movimiento si es asi me apago*/
 
}

def apagarTras(){
	// Mirar si no ha habido ningun cambio en el sensor mientras ha estado dormido el sistema:
    def sigue_inactivo = elSensorMov.currentState("motion"); //con el metodo evaluamos el atributo value
   	if (sigue_inactivo.value=="inactive") {
    
    log.debug "tras la espera apaga"
	elInterruptor.off()
    }
    else{log.debug "ha pasado la espera pero vuelve a ver presencia, el valor es: ${sigue_inactivo}"
    }
    }

/*Esto no haria falta implementado en uno solo, mediante un if y el objeto evento con la caracteristica value como observamos arriba
def movimientoNoDetectadoManejador (evt){
//llamamos a la variable que hemos definido para el switch con su atributo on
 log.debug "movimientoDetectadoManejador called: $evt" //con el dollar contenido de la variable.(Gruby)
 elInterruptor.off()
}*/
