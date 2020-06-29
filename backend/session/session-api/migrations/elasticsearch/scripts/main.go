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

func parseMapping(content []byte) (string, string) {
	lines := strings.Split(string(content), "\n")
	index := strings.Split(lines[0], " ")[1][1:]
	mappings := strings.Join(lines[1:], "\n")

	var mappingsMap map[string]json.RawMessage
	err := json.Unmarshal([]byte(mappings), &mappingsMap)
	if err != nil {
		log.Fatalf("Failed to parse mappings: %s", mappings)
	}

	return index, string(mappingsMap["mappings"])
}

func main() {
	es := createElasticsearchClient()
	ctx := context.Background()
	path, _ := os.Getwd()
	mappingsPath := strings.Join([]string{path, "..", "mappings"}, "/")
	migrationFiles := readMigrationFiles(mappingsPath)
	log.Printf("Reading mappings from path=%s", mappingsPath)

	for _, file := range migrationFiles {
		filePath := strings.Join([]string{mappingsPath, file.Name()}, "/")
		content, _ := ioutil.ReadFile(filePath)
		index, mappings := parseMapping(content)

		res, err := esapi.IndicesCreateRequest{
			Index: index,
		}.Do(ctx, es)

		if err != nil {
			log.Fatalf("Error getting response: %s", err)
		}
		defer res.Body.Close()
		if res.IsError() {
			log.Printf("[%s] Error creating index=%s path=%s", res.Status(), index, filePath)
		} else {
			log.Printf("Put mappings [%s]: %s", index, mappings)

			res, err = esapi.IndicesPutMappingRequest{
				Index: []string{index},
				Body:  strings.NewReader(mappings),
			}.Do(ctx, es)

			if err != nil {
				log.Fatalf("Error getting response: %s", err)
			}
		}
	}

	log.Println("All done!")
}
