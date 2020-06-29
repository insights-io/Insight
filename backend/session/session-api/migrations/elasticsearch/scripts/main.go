package main

import (
	"context"
	"encoding/json"
	"github.com/elastic/go-elasticsearch"
	"github.com/elastic/go-elasticsearch/esapi"
	"io/ioutil"
	"log"
	"os"
	"regexp"
	"strings"
)

var migrationPattern = `^V([0-9]+)__.*$`

func readMigrationFiles(mappingsPath string) []os.FileInfo {
	files, _ := ioutil.ReadDir(mappingsPath)
	var migrationFiles []os.FileInfo
	for _, file := range files {
		matched, _ := regexp.MatchString(migrationPattern, file.Name())
		if matched {
			migrationFiles = append(migrationFiles, file)
		}
	}
	return migrationFiles
}

func createElasticsearchClient() *elasticsearch.Client {
	elasticsearchHosts := os.Getenv("ELASTICSEARCH_HOSTS")
	if len(elasticsearchHosts) == 0 {
		elasticsearchHosts = "http://localhost:9200"
	}
	addresses := strings.Split(elasticsearchHosts, ",")

	cfg := elasticsearch.Config{Addresses: addresses}
	client, _ := elasticsearch.NewClient(cfg)
	return client
}

func parseMigration(content []byte) (string, map[string]json.RawMessage) {
	lines := strings.Split(string(content), "\n")
	index := strings.Split(lines[0], " ")[1][1:]
	body := strings.Join(lines[1:], "\n")

	var migration map[string]json.RawMessage
	err := json.Unmarshal([]byte(body), &migration)
	if err != nil {
		log.Fatalf("Failed to migration body: %s", body)
	}

	return index, migration
}

func indexExists(es *elasticsearch.Client, ctx context.Context, index string) bool {
	res, err := esapi.IndicesExistsRequest{Index: []string{index}}.Do(ctx, es)
	if err != nil {
		log.Fatalf("Error getting response: %s", err)
	}
	defer res.Body.Close()
	if res.IsError() && res.StatusCode != 404 {
		log.Fatalf("[%d] Error checking if index exists", res.StatusCode)
	}
	return res.StatusCode == 200
}

func main() {
	es := createElasticsearchClient()
	ctx := context.Background()
	path, _ := os.Getwd()
	mappingsPath := strings.Join([]string{path, "..", "mappings"}, "/")
	migrationFiles := readMigrationFiles(mappingsPath)
	log.Printf("Searching for migrations in: %s", mappingsPath)

	for _, file := range migrationFiles {
		filePath := strings.Join([]string{mappingsPath, file.Name()}, "/")
		log.Printf("Applying migration from: %s", filePath)

		content, _ := ioutil.ReadFile(filePath)
		index, migration := parseMigration(content)

		if !indexExists(es, ctx, index) {
			log.Printf("Creating index: %s", index)
			res, err := esapi.IndicesCreateRequest{Index: index}.Do(ctx, es)
			if err != nil {
				log.Fatalf("Error getting response: %s", err)
			}
			defer res.Body.Close()
			if res.IsError() {
				log.Printf("[%s] Error creating index=%s path=%s", res.Status(), index, filePath)
				continue
			}
		}

		if len(migration["mappings"]) > 0 {
			mappings := string(migration["mappings"])
			log.Printf("Creating mappings for index: %s %s", index, mappings)

			res, err := esapi.IndicesPutMappingRequest{Index: []string{index}, Body: strings.NewReader(mappings)}.Do(ctx, es)
			if err != nil {
				log.Fatalf("Error getting response: %s", err)
			}
			defer res.Body.Close()
			if res.IsError() {
				log.Printf("[%d] Error creating mapping for index=%s", res.StatusCode, index)
			}

		}
	}

	log.Println("All done!")
}
