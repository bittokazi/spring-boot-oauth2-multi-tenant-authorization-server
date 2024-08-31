
# Multi Tenant Spring OAUTH2 Authorization Server built with Kotlin/Java-Spring and Angular.

## Features

1. Built with **Kotlin**, **Spring Boot** and **Spring Oauth2 Authorization Server library**
2. **Multi Tenant** feature. Tenants can be accessed with different domains
3. Create Tenant with web UI built on top of Angular
4. OpenID Connect support
5. 2FA support
6. Consent support
7. Login/2FA/Consent page custom template for different tenant support.
8. Custom templates can be written in **Thymeleaf** and can be uploaded for each tenant using web UI Admin panel.

## Run using Docker

### if you want to run it out of the box locally follow the below instructions:

- **Clone** the repository
- Go to **root** of the repository
- Build with **docker compose** using the command: `docker-compose build`  
  or if you use **Docker Compose V2** then build command: `docker compose build`
- Run the application with command `docker-compose up `  
  or if you use **Docker Compose V2** then build command `docker compose up`
- If you want to run it in detached mode then: `docker-compose up -d`
- now access the application from web: [http://localhost:5020](http://localhost:5020)
- default **username**: `admin` and **password**: `password`

## Environment Variables
#### To customize please set the environment variables in the `docker.env` file:

# you domain ex: https://example.com APPLICATION_BACKEND_URL=http://localhost:5020
    # not needed to change  
GATEWAY_BACKEND_SERVICE=http://127.0.0.1:5010      
# not needed to change  
GATEWAY_FRONTEND_SERVICE=http://127.0.0.1:4200      
# change it with your db settings  
DB_HOSTNAME=authserverdb    DB_NAME=auth_server_db     
DB_PASSWORD=password      
DB_USERNAME=postgres      
DB_PORT=5432        
DEPLOY_ENV=testing

    # recommended: change it with your secret  
REMEMBER_ME_KEY=secretkey      
# recommended: change it to htts:// if you use ss  
HTTP_SCHEMA=http://      
# recommended: change it with your unique uuid   
KID=92ad669a-93ce-49cc-a258-82a33e679607

    # highly recommended: change it following the instruction in this readme   
    CERT_PRIVATE_KEY_FILE=default_authkit_private_key_pkcs8.pem     
      
     # highly recommended: change it following the instruction in this readme   
    CERT_PUBLIC_KEY_FILE=default_authkit_public_key.pem      
      
    USE_X_AUTH_TENANT=true        
      
    # not needed to change  
CERT_FOLDER_BASE=/certs    # not needed to change    
TEMPLATE_FOLDER_BASE=/template-assets

    # not needed to change  
FE_PORT=4200
#### if you run the application in the intellije then you need to set this environment variables as well.


## Create Certificates

Follow these instructions to create your own certificates.

1. Open terminal and execute the command listed in second point. you  
   can  set your desired filenames here. RECOMMENDED that You  
   save/create    these certificate files in a different directory  
   rather than in this    repository directory, please put the absolute  
   path to the certificate    folder in docker-compose.yml file in the  
   volumes property.

example:

volumes: - /path/to/certificates/directory:/certs

2. Command to create new certificate for oauth2 authorization server, run them one by one:

>     openssl genrsa -out authkit_private_key.pem 4096  

>     openssl rsa -pubout -in authkit_private_key.pem -out authkit_public_key.pem  

>     openssl pkcs8 -topk8 -in authkit_private_key.pem -inform pem -out authkit_private_key_pkcs8.pem -outform pem -nocrypt  

3. Please specify the correct name of the public key and pkcs8 private key name in the "**docker.env**" file  
   replace the below environment variable value with your filename:


CERT_PUBLIC_KEY_FILE=authkit_public_key.pem

CERT_PRIVATE_KEY_FILE=authkit_private_key_pkcs8.pem CERT_PUBLIC_KEY_FILE=authkit_public_key.pem
## Run in Intellije

- Run **gateway** as kotlin application
- Run **spring-boot-oauth2-auth-server** as Kotlin application with the environment variables listed above
- Run **auth-kit-frontend** as an angular app instructions are found in **auth-kit-frontend** folder


## When creating new tenant

- when you create new tenant **a default user** will be created
- credentials for that default user will be,
-  - **username**: `admin`
- -  **password**: `password`
- You can **switch** to **any tenant** from **Main tenant** from the tenant list and add edit update oauth2 clients, users, roles.
- To switch **back to main tenant** just click to your profile icon from top right and click **switch tenant**



## Contributing

Contributions are always welcome!

Specially on how to use the application.

See `contributing.md` for ways to get started.

Please adhere to this project's `code of conduct`.

