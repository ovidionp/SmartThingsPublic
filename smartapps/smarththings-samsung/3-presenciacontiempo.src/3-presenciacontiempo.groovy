/**
 *  3_PresenciaConTiempo
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
    name: "3_PresenciaConTiempo",
    namespace: "SmarthThings_Samsung",
    author: "ovidio navarro",
    description: "Sensor de movimiento vinculado a un switch, evitando rebotes en el switch usando un tiempo para esperar tras presencia no detectada.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Selecciona el sensor de movimiento") {
    
    	input ("tiempoApagar", "number",title:"timpo apagado",  )
        
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
    log.debug "movimiento desactivado-espera ${tiempoApagar} segundos interruptor";
    runIn(tiempoApagar, apagarTras); //Dormirse esos segundos y abrir el manejador
    }
 /*EXTRA, no hay movimiento y me duermo x segundos y al volver veo si no ha habido movimiento si es asi me apago*/
 
}

def apagarTras(){
	// Mirar estado tras el tiempo pasado:
    //sigue_inactivo es un "state"
    def sensor_actual =elSensorMov.currentState("motion"); //estado actual
    def sensor_ultimo_cambio = elSensorMov.latestState("motion"); //con el metodo evaluamos la ultima vez que cambio el atributo
   	//sigue inactivo.value es un string
    if (sensor_actual.value=="inactive") {
    log.debug "esta inactivo, ver si apagar.."
    //Tiempo inactivo---now-(fecha de ultima vez activo), si>tiempoApagar--apagar
    
            if((now()- sensor_ultimo_cambio.date.time)>tiempoApagar){

        log.debug "tras la espera apaga"
        elInterruptor.off()
        }
            else{
                    log.debug "Hace menos del tiempo estuvo encendido"
                    runIn(tiempoApagar, apagarTras);

            }
    }
    else{log.debug "ha pasado la espera pero vuelve a ver presencia, el valor es: ${sigue_inactivo}"
    }
    }

