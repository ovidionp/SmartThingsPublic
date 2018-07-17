/**ESTO SE PROVARIA CON PORTMAN, llamando a la direccion: API ENDPOINT + API TOKEN
 *
 * GET: https://graph.api.smartthings.com/api/smartapps/installations/a92cfa4f-c030-405b-a5d3-854489e47c6d/ switches/access_token=9dc185c9-1680-48e5-b2cb-39388288e72b
 * PUT: https://graph.api.smartthings.com/api/smartapps/installations/a92cfa4f-c030-405b-a5d3-854489e47c6d/ switches/off?access_token=9dc185c9-1680-48e5-b2cb-39388288e72b
 *  ServicioWeb
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
    name: "ServicioWeb",
    namespace: "ovidionp",
    author: "ovidio navarro",
    description: "Crear un servicio web",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Selecciona los dispositivos a los que se les permitirá la consulta:") {
    	input ("elInterruptor","capability.switch",
 					title: "Selecciona luces?", required: true, multiple: false)
		// TODO: put inputs here
	}
}

                   
            
def installed() {
	log.debug "Installed with settings: ${settings}"

	
}


def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	
}

mappings {
	//Se asocia el punto de acceso "/switches a:"
	path("/switches"){
    
        action: [ GET: "listSwitches"] //ejecutar el siguiente manejador
        }
    //indicando ":command" indicamos que esa parte es variable pudiendo ser on o off
    // y será como lo pongamos con el put
    path("/switches/:command"){
    	action:[ PUT: "updateSwitches"]
        }
}


def updateSwitches(){
//params contine información sobre la petición (lo que recibimos) 
	def cmd=params.command
    log.debug "command: $cmd"
    switch(cmd){
    	case "on":
        	//manajador para el on
            elInterruptor.on();
        	break;
        case "off":
        	//manejador para el off
        	elInterruptor.off();

            
        	break;
         default:
         	httpError(501,"$command is not a valid command dor all switches specified")
      }
    
}

//return una lista como : [[name: "lamapara cocina", value: "off"], [name: "lampara baño", value: "on"]]
def listSwitches(){
	def resp=[] //inicializar array resp
    elInterruptor.each{	
        resp<<[name: it.displayName, value: it.currentValue("switch")] 
    }
    return resp;
}