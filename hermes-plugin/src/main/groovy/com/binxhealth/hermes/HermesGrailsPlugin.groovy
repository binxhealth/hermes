package com.binxhealth.hermes

import grails.plugins.*

class HermesGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.5 > *"

    def title = "Hermes" // Headline display name of the plugin
    def author = "Maura Warner"
    def authorEmail = "maura.warner@atlasgenetics.com"
    def description = '''\
An HTTP messenger that sends REST requests to third party APIs with guaranteed one-time delivery
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "https://github.com/atlasgenetics/hermes/blob/master/README.md"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Atlas Genetics", url: "http://atlasgenetics.com" ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Git", url: "https://github.com/atlasgenetics/hermes/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/atlasgenetics/hermes" ]

}
