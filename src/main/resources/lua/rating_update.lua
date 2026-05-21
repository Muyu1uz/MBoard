local userHashKey = KEYS[1]
local aggKey = KEYS[2]
local hotKey = KEYS[3]
local userId = ARGV[1]
local newStar = tonumber(ARGV[2])
local hotDelta = tonumber(ARGV[3])

local oldValue = redis.call('HGET', userHashKey, userId)
local oldStar = 0
if oldValue ~= false then
  oldStar = tonumber(oldValue)
end

local count = tonumber(redis.call('HGET', aggKey, 'count') or '0')
local sum = tonumber(redis.call('HGET', aggKey, 'sum') or '0')

redis.call('HSET', userHashKey, userId, newStar)

if oldStar == 0 then
  count = count + 1
  sum = sum + newStar
else
  sum = sum - oldStar + newStar
end

local avg = 0
if count > 0 then
  avg = sum / count
end

redis.call('HSET', aggKey, 'count', count, 'sum', sum, 'avg', avg)

if hotDelta ~= 0 then
  redis.call('ZINCRBY', hotKey, hotDelta, string.match(aggKey, '(%d+)$'))
end

return {oldStar, count, sum, avg}
