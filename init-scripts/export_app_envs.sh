#! /bin/sh

APP_SECRETS_FOLDER=/var/run/secrets/nais.io/vault/su-inntekt
[ -d "$APP_SECRETS_FOLDER" ] && {
    . "$APP_SECRETS_FOLDER"/*.env
    export $(cut -d= -f1 "$APP_SECRETS_FOLDER"/*.env)
}
