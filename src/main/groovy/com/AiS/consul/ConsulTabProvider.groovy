package com.AiS.consul

import com.morpheusdata.core.AbstractInstanceTabProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Account
import com.morpheusdata.model.Instance
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.ContentSecurityPolicy
import com.morpheusdata.model.User
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel
import com.morpheusdata.core.util.RestApiUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.reactivex.Single
import groovy.util.logging.Slf4j

import java.time.Instant


@Slf4j
class ConsulTabProvider extends AbstractInstanceTabProvider{
    Plugin plugin
    MorpheusContext morpheus
    RestApiUtil consulAPI


    String code ="hashicorp-consul-tab"
    String name ="HashiCorp Consul"

    ConsulTabProvider(Plugin plugin, MorpheusContext context) {
        this.plugin = plugin
        this.morpheus = context
        this.consulAPI= new RestApiUtil()
    }

    ConsulTabProvider(Plugin plugin, MorpheusContext morpheusContext, RestApiUtil consulAPI) {
        this.plugin = plugin
        this.morpheusContext = morpheusContext
        this.consulAPI = api
    }

    @Override
    HTMLResponse renderTemplate(Instance instance) {
        ViewModel<Instance> model = new ViewModel<>()
        TaskConfig config = morpheus.buildInstanceConfig(instance,[:],null,[],[:]).blockingGet()
        try {
            def settings =morpheus.getSettings(plugin)
            def settingsOutput=""
            settings.subscribe(
                    {
                        outData->
                            settingsOutput=outData
                    },
                    {
                        error ->
                            println error.printStackTrace()
                    }
            )

            JsonSlurper slurper = new JsonSlurper()
            def settingsJson = jsonSlurper.parseText(settingsOutput)
            def HashMap<String,String> consulPayLoad = new HashMap<String,String>()
            consulPayLoad.put("name",instance.name)
            def result = consulAPI.callApi(settingsJson.consulServerUrl, "v1/health/node/${instance.name}","","", new RestApiUtil.RestOptions(headers:["content-type":"application/json"], ignoreSSL: false),"GET")
            def json = slurper.parseText(result.content)
            if (json.size==0){
                getRenderer.renderTemplate("hbs/instanceNotFoundTab", model)
            }
            else {
                def status= json[0].Status
                consulPayLoad.put("status":status)
                def nodeServices=consulAPI.callApi(settingsJson.consulServerUrl, "v1/catalog/node-services/${instance.name}","","", new RestApiUtil.RestOptions(headers:["content-type":"application/json"], ignoreSSL: false),"GET")
                def servicesJson= slurper.parseText(nodeServices.content)
                def servies=servicesJson
                if (servies.Services=="null"){
                    consulPayLoad.put("servicesCount",0)
                }
                else {
                    consulPayLoad.put("servicesCount",servies.Services)
                }
                consulPayLoad.put("datacenter",servies.Node.Datacenter)
                consulPayLoad.put("services",servies.Services.size)
                consulPayLoad.put("metadata",servies.Node.Meta)
                model.object=consulPayLoad
                getRenderer.renderTemplate("hbs/instanceTab", model)
            }
        }
        catch (Exception ex){
            println( "Error parsing the Consul plugin Settings. Ensure that the Plugin Settings")
            getRenderer.renderTemplate("hbs/instanceNotFoundTab", model)
        }
    }

    @Override
    Boolean show(Instance instance, User user, Account account) {
        def show =false
        if (user.permission["consul-integration"]==full){
            show=true
        }
        return show
    }


    @Override
    ContentSecurityPolicy getContentSecurityPolicy(){
        def csp= new ContentSecurityPolicy()
        csp
    }
}
