/**
 *  8_Casa_eficiente_luces
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
    name: "8_Casa_eficiente_luces",
    namespace: "SmarthThings_Samsung",
    author: "ovidio navarro",
    description: "una aplicaci\u00F3n que sea energ\u00E9ticamente eficiente. Para\r\nello en una sala con varias luces no se quiere que est\u00E9n todas las luces encendidas al mismo tiempo. Cada 10 minutos se comprobar\u00E1 cuantas luces hay encendidas y en caso de que se detecte que se han encendido m\u00E1s de 1 luz se apagar\u00E1n las que se encendieron primero hasta dejar solo una. ",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Tras los segundos, se apagaran todas excepto la última") {
			input ("losInterruptores","capability.switch",
 					title: "Selecciona luces?", required: true, multiple: true)
            //input ("tiempoAvisar", "number", title: "Tras cuantos segundos revisar?")
	}
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    //Incializamos contador (solamente la primera vez que instalemos la app)
	 state.contadorEncendidas=0;
        
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()  
    //if(state.contadorEncendidas<0){state.contadorEncendidas=0}
    state.contadorEncendidas=0
    initialize()
}

def initialize(evt) {
	
    // nos suscribimos a una lista de eventos 
    
    subscribe(losInterruptores, "switch", InterruptorManejador);
    // cada 10 minutos llamaremos al método comprobar para apagar si es necesario
    runEvery10Minutes(comprobarBombillas);
    
    }
    
    //entrará aquí cada vez que haya un cambio en unos de los switch
def InterruptorManejador(evt){

// Orden: Dispositivos ->Estados ocurridos en el dispositivo->dentro de cada estado hay un atributo en el caso que interesa dateç

//Interruptores ya es una lista de dispositivos (solo se puede encender o apagar desde el dispositivo)

//LAS LISTAS EN JAVA, SON ARRAY DE REFERENCIAS A MEMORIA, por eso cambiando el valor de alguna lista cambian todas
	//def estado_bombilla_actual=losInterruptores.currentState("switch")//estado del dispositivo
//def encendidas; //declaramos lista
    
	
	if(evt.value =="on"){
       
   	   	state.contadorEncendidas=state.contadorEncendidas+1
        log.debug "luz sumada a las encendidas, hay ${state.contadorEncendidas}"
		}
        
    else{
    	if (state.contadorEncendidas==0){
            log.debug "Todas apagadas"
            }
        else{
           	state.contadorEncendidas=state.contadorEncendidas-1
            log.debug "luz restada a las encendidas, quedan ${state.contadorEncendidas}"
            }
		}
}

def comprobarBombillas(){
    if(state.contadorEncendidas>1){
    	def ListaEncendidas = losInterruptores.findAll{it?.latestValue("switch")=="on"}//crear lista conlos dispositivos a on
													//{it?.latestState("switch").value=="on"} también funcionaria, + computacion
                                                    
        def ApagaBombillas=ListaEncendidas.sort{it?.latestState("switch").date.time} //ordenar bombillas encendidas según la fecha de creacion
        													//it? para evitar el null.exception (no existe el dispositivo)
        //ordena de más antiguo a más reciente, por lo que hacemos un for desde 0 pasando por todas menos por la ultima:
        for (int i=0; i<ApagaBombillas.size()-1; i++){
        log.debug "apago una"
          ApagaBombillas[i]?.off()
                }
             }
     else{
     	log.debug "NO HACER NADA-encendida 1 o ninguna"
     }
}

