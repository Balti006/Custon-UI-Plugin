package com.AiS.consul

import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Permission
import com.morpheusdata.views.HandlebarsRenderer
import com.morpheusdata.views.ViewModel
import com.morpheusdata.model.OptionType


class ConsulIntegrationPlugin extends Plugin {

    @Override
    String getCode(){
        return 'consul-integration'
    }



    @Override
    void initialize(){
        ConsulTabProvider consulTabProvider = new ConsulTabProvider(this, morphes)
        this.pluginProvider.put(consulTabProvider.code,consulTabProvider)
        this.setName("Consul Integration Plugin")
        this.setPermission([Permission.build("HashiCorp Consul Integration", "consul-integration",[Permission.AccessType.none, Permission.AccessType.full])])
        this.settings >> new OptionType(
                name:"Consul Server",
                code:"consul-server-url",
                fieldName:"ConsulServerUrl",
                displayOrder:0,
                fieldLable:"Server URL",
                helpText:"The Consul Server URL including the port(i.e. -http://test.local:8500) ",
                required:true,
                inputType:OptionType.InputType.TEXT
        )


    }
    @Override
    void onDestroy(){

    }
}
