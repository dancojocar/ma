PORT ?= 3000
BASE := http://localhost:$(PORT)
API  := $(BASE)/api/spots
CURL := curl -sS -H 'Content-Type: application/json' -H "Idempotency-Key: make-$$(date +%s)-$$RANDOM"

JQ_ARGS   :=
JQ_FILTER := {}
ifneq ($(NAME),)
JQ_ARGS   += --arg name "$$NAME"
JQ_FILTER += | .name = $$name
endif
ifneq ($(CATEGORY),)
JQ_ARGS   += --arg category "$$CATEGORY"
JQ_FILTER += | .category = $$category
endif
ifneq ($(RATING),)
JQ_ARGS   += --argjson rating '$(RATING)'
JQ_FILTER += | .rating = $$rating
endif
ifneq ($(PRICE),)
JQ_ARGS   += --argjson price '$(PRICE)'
JQ_FILTER += | .priceLevel = $$price
endif
ifneq ($(LAT),)
JQ_ARGS   += --argjson lat '$(LAT)'
JQ_FILTER += | .lat = $$lat
endif
ifneq ($(LNG),)
JQ_ARGS   += --argjson lng '$(LNG)'
JQ_FILTER += | .lng = $$lng
endif
ifneq ($(OPEN),)
JQ_ARGS   += --argjson open '$(OPEN)'
JQ_FILTER += | .openNow = $$open
endif
ifneq ($(PHOTO),)
JQ_ARGS   += --arg photo "$$PHOTO"
JQ_FILTER += | .photoUrl = $$photo
endif
ifneq ($(DESCRIPTION),)
JQ_ARGS   += --arg description "$$DESCRIPTION"
JQ_FILTER += | .description = $$description
endif
export NAME CATEGORY PHOTO DESCRIPTION
BODY = $$(jq -nc '$(JQ_FILTER)' $(JQ_ARGS))

.PHONY: help start dev test list get create update rename open close delete watch

help:
	@echo "Server:"
	@echo "  make start | dev | test"
	@echo ""
	@echo "Spots (PORT=$(PORT); fields: NAME CATEGORY RATING PRICE LAT LNG OPEN PHOTO DESCRIPTION):"
	@echo "  make list"
	@echo "  make get    ID=spot-1"
	@echo "  make create NAME='Live Burger' CATEGORY=fastfood [RATING=4.5 PRICE=1 OPEN=true ...]"
	@echo "  make update ID=spot-1 NAME='Pizza Stop (new oven)' [OPEN=false ...]"
	@echo "  make rename ID=spot-1 NAME='New name'"
	@echo "  make open   ID=spot-1        make close ID=spot-1"
	@echo "  make delete ID=<uuid>"
	@echo ""
	@echo "Live:"
	@echo "  make watch   # print every WebSocket event from $(BASE)/live"
	@echo ""
	@echo "Categories: cafe canteen fastfood bakery bar   Price: 1 2 3   Rating: 0-5"

start:
	npm start

dev:
	npm run dev

test:
	npm test

list:
	@curl -sS $(API) | jq '.spots[] | {id, name, category, openNow, rating}'

get: require-id
	@curl -sS $(API)/$(ID) | jq .

create:
	@test -n "$$NAME" -a -n "$$CATEGORY" || { echo "usage: make create NAME='...' CATEGORY=cafe|canteen|fastfood|bakery|bar [RATING= PRICE= LAT= LNG= OPEN= PHOTO= DESCRIPTION=]"; exit 1; }
	@$(CURL) -X POST $(API) -d "$(BODY)" | jq .

update: require-id
	@test "$(BODY)" != '{}' || { echo "usage: make update ID=spot-1 NAME='...' [CATEGORY= RATING= PRICE= LAT= LNG= OPEN= PHOTO= DESCRIPTION=]"; exit 1; }
	@$(CURL) -X PATCH $(API)/$(ID) -d "$(BODY)" | jq .

rename: require-id
	@test -n "$$NAME" || { echo "usage: make rename ID=spot-1 NAME='New name'"; exit 1; }
	@$(MAKE) --no-print-directory update ID=$(ID)

open: require-id
	@$(MAKE) --no-print-directory update ID=$(ID) OPEN=true

close: require-id
	@$(MAKE) --no-print-directory update ID=$(ID) OPEN=false

delete: require-id
	@$(CURL) -o /dev/null -w 'HTTP %{http_code}\n' -X DELETE $(API)/$(ID)

watch:
	npx wscat -c ws://localhost:$(PORT)/live

require-id:
	@test -n "$(ID)" || { echo "ID is required, e.g. make $(MAKECMDGOALS) ID=spot-1 (see: make list)"; exit 1; }
