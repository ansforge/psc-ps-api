job "psc-api-maj-v2" {
  datacenters = [
    "${datacenter}"]
  type = "service"
  namespace = "${nomad_namespace}"
  
  vault {
    policies = [
      "psc-ecosystem"]
    change_mode = "restart"
  }

  affinity {
    attribute = "$\u007Bnode.class\u007D"
    value = "standard"
  }

  group "psc-api-maj-v2" {
    count = "1"
    restart {
      attempts = 3
      delay = "60s"
      interval = "1h"
      mode = "fail"
    }

    network {
      port "http" {
        to = 8080
      }
      port "filebeat" {
        to = 5066
      }
    }

    scaling {
      enabled = true
      min = 1
      max = 5

      policy {
        cooldown = "180s"
        check "few_requests" {
          source = "prometheus"
          query = "min(max(http_server_requests_seconds_max{_app='psc-api-maj-v2'}!= 0)by(instance))"
          strategy "threshold" {
            upper_bound = 2
            delta = -1
          }
        }

        check "many_requests" {
          source = "prometheus"
          query = "min(max(http_server_requests_seconds_max{_app='psc-api-maj-v2'}!= 0)by(instance))"
          strategy "threshold" {
            lower_bound = 0.5
            delta = 1
          }
        }
      }
    }

    task "psc-api-maj-v2" {
      driver = "docker"
      config {
        image = "${artifact.image}:${artifact.tag}"
        ports = [
          "http"]
      }

      template {
        destination = "local/file.env"
        env = true
        data = <<EOH
PUBLIC_HOSTNAME={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.admin_public_hostname }}{{ end }}
JAVA_TOOL_OPTIONS="-Xms256m -Xmx2g -XX:+UseG1GC -Dspring.config.location=/secrets/application.properties"
EOH
      }

      template {
        data = <<EOF
spring.application.name=psc-api-maj
server.servlet.context-path=/psc-api-maj
logging.level.org.springframework.data.mongodb.core.MongoTemplate=INFO
server.error.include-stacktrace=never
spring.data.mongodb.host={{ range service "${nomad_namespace}-psc-mongodb" }}{{ .Address }}{{ end }}
spring.data.mongodb.port={{ range service "${nomad_namespace}-psc-mongodb" }}{{ .Port }}{{ end }}
spring.data.mongodb.database=mongodb
{{ with secret "psc-ecosystem/${nomad_namespace}/mongodb" }}spring.data.mongodb.username={{ .Data.data.root_user }}
spring.data.mongodb.password={{ .Data.data.root_pass }}{{ end }}
spring.data.mongodb.auto-index-creation=false
{{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}logging.level.fr.ans.psc={{ .Data.data.log_level }}{{ end }}
EOF
        destination = "secrets/application.properties"
        change_mode = "restart"
      }

      resources {
        cpu = 500
        memory = 2560
      }


      service {
        name = "$\u007BNOMAD_NAMESPACE\u007D-$\u007BNOMAD_JOB_NAME\u007D"
        tags = ["urlprefix-$\u007BPUBLIC_HOSTNAME\u007D/psc-api-maj"]
        port = "http"
        check {
          type = "tcp"
          port = "http"
          interval = "30s"
          timeout = "2s"
          failures_before_critical = 5
        }
      }

      service {
        name = "metrics-exporter"
        port = "http"
        tags = [
          "_endpoint=/psc-api-maj/v2/actuator/prometheus",
          "_app=psc-api-maj-v2",]
      }
    }

    task "log-shipper" {
      driver = "docker"
      restart {
        interval = "30m"
        attempts = 5
        delay = "15s"
        mode = "delay"
      }
      meta {
        INSTANCE = "$\u007BNOMAD_ALLOC_NAME\u007D"
      }
      template {
        data = <<EOH
LOGSTASH_HOST = {{ range service "${nomad_namespace}-logstash" }}{{ .Address }}:{{ .Port }}{{ end }}
ENVIRONMENT = "${datacenter}"
EOH
        destination = "local/file.env"
        env = true
      }
      config {
        image = "prosanteconnect/filebeat:7.17.0"
        ports = [
          "filebeat"]
      }
        service {
        name = "log-shipper"
        port = "filebeat"
        check {
          type = "tcp"
          port = "filebeat"
          interval = "30s"
          timeout = "2s"
        }
      }
    }
  }
}
