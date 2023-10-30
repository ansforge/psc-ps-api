project = "${workspace.name}/psc-api-maj-v2"

# Labels can be specified for organizational purposes.
labels = {
  "domaine" = "psc"
}

runner {
  enabled = true
  profile = "secpsc-${workspace.name}"
  data_source "git" {
    url = "https://github.com/prosanteconnect/psc-api-maj-v2.git"
    ref = "${workspace.name}"
  }
  poll {
    enabled = false
  }
}

# An application to deploy.
app "prosanteconnect/psc-api-maj-v2" {
  # Build specifies how an application should be deployed. In this case,
  # we'll build using a Dockerfile and keeping it in a local registry.
  build {
    use "docker" {
      build_args = {
        "proxy_address" = var.proxy_address
      }
      dockerfile = "${path.app}/${var.dockerfile_path}"
    }
    # Uncomment below to use a remote docker registry to push your built images.
    registry {
      use "docker" {
        image = "${var.registry_username}/psc-api-maj"
        tag = gitrefpretty()
        username = var.registry_username
        password = var.registry_password
	local = true
      }
    }
  }

  # Deploy to Nomad
  deploy {
    use "nomad-jobspec" {
      jobspec = templatefile("${path.app}/psc-api-maj.nomad.tpl", {
        datacenter = var.datacenter
	nomad_namespace = var.nomad_namespace
      })
    }
  }
}

variable "datacenter" {
  type = string
  default = ""
  env = ["NOMAD_DATACENTER"]
}

variable "nomad_namespace" {
  type = string
  default = ""
  env = ["NOMAD_NAMESPACE"]
}

variable "registry_username" {
  type    = string
  default = ""
  env     = ["REGISTRY_USERNAME"]
  sensitive = true
}

variable "registry_password" {
  type    = string
  default = ""
  env     = ["REGISTRY_PASSWORD"]
  sensitive = true
}

variable "proxy_address" {
  type = string
  default = ""
}

variable "dockerfile_path" {
  type = string
  default = "Dockerfile"
}
