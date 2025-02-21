
# Why are we using a Makefile? PactFlow has around 30 example consumer and provider projects that show how to use Pact.
# We often use them for demos and workshops, and Makefiles allow us to provide a consistent language and platform agnostic interface
# for each project. You do not need to use Makefiles to use Pact in your own project!

# Default to the read only token - the read/write token will be present on Travis CI.
# It's set as a secure environment variable in the .travis.yml file
GITHUB_ORG="andreriosdefreitas"
PACTICIPANT="consumer-client"
GITHUB_WEBHOOK_UUID := "5076f253-8193-4ccd-8304-e99a9a76a0ab"
PACT_BROKER_URL := https://wellhub-84f2ecdb.pactflow.io/
PACT_BROKER_TOKEN:= ${PACT_BROKER_TOKEN}
PACT_CLI="docker run --rm -v ${PWD}:${PWD} -e PACT_BROKER_URL -e PACT_BROKER_TOKEN pactfoundation/pact-cli"
VERSION?=$(shell git rev-parse --short HEAD)
GIT_BRANCH?=$(shell git rev-parse --abbrev-ref HEAD)
ENVIRONMENT?=test

# Only deploy from master (to production env) or test (to test env)
ifeq ($(GIT_BRANCH),main)
	ENVIRONMENT=test
	DEPLOY_TARGET=deploy
else
	ifeq ($(GIT_BRANCH),test)
		ENVIRONMENT=test
		DEPLOY_TARGET=deploy
	else
		DEPLOY_TARGET=no_deploy
	endif
endif

all: test

## ====================
## CI tasks
## ====================

ci: test publish_pacts can_i_deploy $(DEPLOY_TARGET)

# Run the ci target from a developer machine with the environment variables
# set as if it was on CI.
# Use this for quick feedback when playing around with your workflows.
fake_ci: .env
	@CI=true \
	API_BASE_URL=http://localhost:8080 \
	make ci

# publish_pacts: .env
# 	@echo "\n========== STAGE: publish pacts ==========\n"
# 	@"${PACT_CLI}" publish ${PWD}/build/pacts --consumer-app-version ${VERSION} --branch ${GIT_BRANCH}

## =====================
## Build/test tasks
## =====================

test: .env
	@echo "\n========== STAGE: test (pact) ==========\n"
	./gradlew build
	@echo "\n========== STAGE: pactPublish  ==========\n"
	./gradlew pactPublish


## =====================
## Deploy tasks
## =====================

create_environment:
	@"${PACT_CLI}" broker create-environment --name production --production

deploy: deploy_app record_deployment

no_deploy:
	@echo "Not deploying as not on main branch"

can_i_deploy: .env
	@echo "\n========== STAGE: can-i-deploy? ==========\n"
	@"${PACT_CLI}" broker can-i-deploy \
	  --pacticipant ${PACTICIPANT} \
	  --version ${VERSION} \
	  --to-environment ${ENVIRONMENT} \
	  --retry-while-unknown 30 \
	  --retry-interval 10 \
	  --broker-base-url ${PACT_BROKER_URL}

deploy_app:
	@echo "\n========== STAGE: deploy ==========\n"
	@echo "Deploying to ${ENVIRONMENT}"

record_deployment: .env
	@"${PACT_CLI}" broker record_deployment --pacticipant ${PACTICIPANT} --version ${VERSION} --environment ${ENVIRONMENT} --broker-base-url ${PACT_BROKER_URL}

## =====================
## PactFlow set up tasks
## =====================

# This should be called once before creating the webhook
# with the environment variable GITHUB_TOKEN set
create_github_token_secret:
	@curl -v -X POST ${PACT_BROKER_BASE_URL}/secrets \
	-H "Authorization: Bearer ${PACT_BROKER_TOKEN}" \
	-H "Content-Type: application/json" \
	-H "Accept: application/hal+json" \
	-d  "{\"name\":\"githubCommitStatusToken\",\"description\":\"Github token for updating commit statuses\",\"value\":\"${GITHUB_TOKEN}\"}"

# This webhook will update the Github commit status for this commit
# so that any PRs will get a status that shows what the status of
# the pact is.
create_or_update_github_commit_status_webhook:
	@"${PACT_CLI}" \
	  broker create-or-update-webhook \
	  'https://api.github.com/repos/andreriosdefreitas/pact-consumer/statuses/$${pactbroker.consumerVersionNumber}' \
	  --header 'Content-Type: application/json' 'Accept: application/vnd.github.v3+json' 'Authorization: token $${user.githubCommitStatusToken}' \
	  --request POST \
	  --data @${PWD}/pactflow/github-commit-status-webhook.json \
	  --uuid ${GITHUB_WEBHOOK_UUID} \
	  --consumer ${PACTICIPANT} \
	  --contract-published \
	  --provider-verification-published \
	  --description "Github commit status webhook for ${PACTICIPANT}"

test_github_webhook:
	@curl -v -X POST ${PACT_BROKER_BASE_URL}/webhooks/${GITHUB_WEBHOOK_UUID}/execute -H "Authorization: Bearer ${PACT_BROKER_TOKEN}"


## ======================
## Misc
## ======================

.env:
	touch .env
