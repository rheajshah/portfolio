/**************************************************************************
  * C S 429 system emulator
 * 
 * cache.c - A cache simulator that can replay traces from Valgrind
 *     and output statistics such as number of hits, misses, and
 *     evictions, both dirty and clean.  The replacement policy is LRU. 
 *     The cache is a writeback cache. 
 * 
 * Copyright (c) 2021, 2023. 
 * Authors: M. Hinton, Z. Leeper.
 * All rights reserved.
 * May not be used, modified, or copied without permission.
 **************************************************************************/ 
#include <getopt.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <assert.h>
#include <limits.h>
#include <string.h>
#include <errno.h>
#include "cache.h"

#define ADDRESS_LENGTH 64

/* Counters used to record cache statistics in printSummary().
   test-cache uses these numbers to verify correctness of the cache. */

//Increment when a miss occurs
int miss_count = 0;

//Increment when a hit occurs
int hit_count = 0;

//Increment when a dirty eviction occurs
int dirty_eviction_count = 0;

//Increment when a clean eviction occurs
int clean_eviction_count = 0;

/* STUDENT TO-DO: add more globals, structs, macros if necessary */
uword_t next_lru;
uword_t tag;
__uint64_t b;
__uint64_t s;
__uint64_t t;
__uint64_t S;
__uint64_t m;
__uint64_t offset;
__uint64_t index_val;


// log base 2 of a number.
// Useful for getting certain cache parameters
static size_t _log(size_t x) {
  size_t result = 0;
  while(x>>=1)  {
    result++;
  }
  return result;
}

/*
 * Initialize the cache according to specified arguments
 * Called by cache-runner so do not modify the function signature
 *
 * The code provided here shows you how to initialize a cache structure
 * defined above. It's not complete and feel free to modify/add code.
 */
cache_t *create_cache(int A_in, int B_in, int C_in, int d_in) {
    /* see cache-runner for the meaning of each argument */
    cache_t *cache = malloc(sizeof(cache_t));
    cache->A = A_in;
    cache->B = B_in;
    cache->C = C_in;
    cache->d = d_in;
    __uint64_t S = cache->C / (cache->A * cache->B);

    cache->sets = (cache_set_t*) calloc(S, sizeof(cache_set_t));
    for (unsigned int i = 0; i < S; i++){
        cache->sets[i].lines = (cache_line_t*) calloc(cache->A, sizeof(cache_line_t));
        for (unsigned int j = 0; j < cache->A; j++){
            cache->sets[i].lines[j].valid = 0;
            cache->sets[i].lines[j].tag   = 0;
            cache->sets[i].lines[j].lru   = 0;
            cache->sets[i].lines[j].dirty = 0;
            cache->sets[i].lines[j].data  = calloc(cache->B, sizeof(byte_t));
        }
    }

    next_lru = 0;
    return cache;
}

cache_t *create_checkpoint(cache_t *cache) {
    unsigned int S = (unsigned int) cache->C / (cache->A * cache->B);
    cache_t *copy_cache = malloc(sizeof(cache_t));
    memcpy(copy_cache, cache, sizeof(cache_t));
    copy_cache->sets = (cache_set_t*) calloc(S, sizeof(cache_set_t));
    for (unsigned int i = 0; i < S; i++) {
        copy_cache->sets[i].lines = (cache_line_t*) calloc(cache->A, sizeof(cache_line_t));
        for (unsigned int j = 0; j < cache->A; j++) {
            memcpy(&copy_cache->sets[i].lines[j], &cache->sets[i].lines[j], sizeof(cache_line_t));
            copy_cache->sets[i].lines[j].data = calloc(cache->B, sizeof(byte_t));
            memcpy(copy_cache->sets[i].lines[j].data, cache->sets[i].lines[j].data, sizeof(byte_t));
        }
    }
    
    return copy_cache;
}

void display_set(cache_t *cache, unsigned int set_index) {
    unsigned int S = (unsigned int) cache->C / (cache->A * cache->B);
    if (set_index < S) {
        cache_set_t *set = &cache->sets[set_index];
        for (unsigned int i = 0; i < cache->A; i++) {
            printf ("Valid: %d Tag: %llx Lru: %lld Dirty: %d\n", set->lines[i].valid, 
                set->lines[i].tag, set->lines[i].lru, set->lines[i].dirty);
        }
    } else {
        printf ("Invalid Set %d. 0 <= Set < %d\n", set_index, S);
    }
}

/*
 * Free allocated memory. Feel free to modify it
 */
void free_cache(cache_t *cache) {
    unsigned int S = (unsigned int) cache->C / (cache->A * cache->B);
    for (unsigned int i = 0; i < S; i++){
        for (unsigned int j = 0; j < cache->A; j++) {
            free(cache->sets[i].lines[j].data);
        }
        free(cache->sets[i].lines);
    }
    free(cache->sets);
    free(cache);
}

/* STUDENT TO-DO:
 * Get the line for address contained in the cache
 * On hit, return the cache line holding the address
 * On miss, returns NULL
 */
cache_line_t *get_line(cache_t *cache, uword_t addr) {
    uword_t S = cache->C / (cache->A * cache->B);
    uword_t s = _log(S);
    uword_t b = _log(cache->B);
    //uword_t m = sizeof(addr) * 8;
    //uword_t t = m - s - b;
    uword_t set_index = (addr >> b) & ((1 << s) - 1); 
    uword_t tag = addr >> (s + b);

    /*offset = addr & (cache->B - 1);
    addr = addr >> b;
    index_val = addr & (S - 1);
    addr = addr >> s;
    tag = (uword_t) addr & ((_exponent(2, t)) -1);*/

    cache_set_t* set = &(cache->sets[set_index]);
    //cache_line_t* line = set->lines;
    //cache_line_t* ret;

    for(unsigned int x = 0; x < cache->A; x++){
        cache_line_t* line = &(set->lines[x]);
        if(line->valid == 1 && line->tag == tag){
            /*ret = line;
            ret->lru = next_lru;
            next_lru++;*/
            return line;
        }
        //line = line + 1;
    }

    return NULL;
}


/* STUDENT TO-DO:
 * Select the line to fill with the new cache line
 * Return the cache line selected to filled in by addr
 */
cache_line_t *select_line(cache_t *cache, uword_t addr) {

    uword_t S = cache->C / (cache->A * cache->B);
    uword_t s = _log(S);
    uword_t b = _log(cache->B);
    uword_t set_index = (addr >> b) & ((1 << s) - 1); 

    cache_set_t* cset = &(cache->sets[set_index]);

    //cache_line_t* line = set->lines;

    uword_t min_lru = next_lru;
    cache_line_t* smallest_lru_line;

    for (unsigned int y = 0; y < cache->A; y++) {
        if(!cset->lines[y].valid){ //is not valid
            return &cset->lines[y];
        } else{
             if (cset->lines[y].lru < min_lru) {
                min_lru = cset->lines[y].lru;
                smallest_lru_line = &cset->lines[y];
            }
        }
    }

    return smallest_lru_line;
}

/*  STUDENT TO-DO:
 *  Check if the address is hit in the cache, updating hit and miss data.
 *  Return true if pos hits in the cache.
 */
bool check_hit(cache_t *cache, uword_t addr, operation_t operation) {
    cache_line_t *line = get_line(cache, addr);

    if (line != NULL) {
        hit_count++;
        line->lru = next_lru;
        next_lru++;
        
        if (operation == WRITE) {
            line->dirty = 1;
        }

        return true;
    } else {
        miss_count++;
        return false;
    }
}

/*  STUDENT TO-DO:
 *  Handles Misses, evicting from the cache if necessary.
 *  Fill out the evicted_line_t struct with info regarding the evicted line.
 */
evicted_line_t *handle_miss(cache_t *cache, uword_t addr, operation_t operation, byte_t *incoming_data) {
    evicted_line_t *evicted_line = malloc(sizeof(evicted_line_t));
    evicted_line->data = (byte_t *) calloc(cache->B, sizeof(byte_t));

    uword_t S = (uword_t) cache->C / (cache->A * cache->B); 
    uword_t s = _log(S); 
    uword_t b = _log(cache->B); 

    uword_t s_index_val = ((addr >> b) & (uword_t) ((1 << s) - 1));

    cache_line_t *s_line = select_line(cache, addr);

    evicted_line->dirty = s_line->dirty;

    evicted_line->addr = (s_line->tag << (b + s)) + (s_index_val << b);

    memcpy(evicted_line->data, s_line->data, cache->B);
    evicted_line->valid = s_line->valid;
    s_line->lru = next_lru;
    next_lru++;
    
    if(s_line->valid){
        if(s_line->dirty){
            dirty_eviction_count++;
            //evicted_line->dirty = 1;
        } else {
            clean_eviction_count++;
            //evicted_line->dirty = 0;
        }
    }

    s_line->valid = true;
    s_line->tag = addr >> (s + b);
    s_line->dirty = (operation == WRITE) ? 1 : 0;

    if(incoming_data != NULL){
        memcpy(s_line->data, incoming_data, cache->B);
    }
    
    return evicted_line;
}

/* STUDENT TO-DO:
 * Get 8 bytes from the cache and write it to dest.
 * Preconditon: addr is contained within the cache.
 */
void get_word_cache(cache_t *cache, uword_t addr, word_t *dest) {
    memcpy(dest, (uword_t*)(get_line(cache, addr)->data + (addr % cache->B)), sizeof(uword_t)); //dest, src, size
}

/* STUDENT TO-DO:
 * Set 8 bytes in the cache to val at pos.
 * Preconditon: addr is contained within the cache.
 */
void set_word_cache(cache_t *cache, uword_t addr, word_t val) {
     memcpy((uword_t*)(get_line(cache, addr)->data + (addr % cache->B)), &val, sizeof(uword_t)); //dest, src, size
}

/*
 * Access data at memory address addr
 * If it is already in cache, increase hit_count
 * If it is not in cache, bring it in cache, increase miss count
 * Also increase eviction_count if a line is evicted
 *
 * Called by cache-runner; no need to modify it if you implement
 * check_hit() and handle_miss()
 */
void access_data(cache_t *cache, uword_t addr, operation_t operation)
{
    if(!check_hit(cache, addr, operation))
        free(handle_miss(cache, addr, operation, NULL));
}
