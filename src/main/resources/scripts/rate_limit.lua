local limit    = tonumber(ARGV[1])
local window   = tonumber(ARGV[2])
local elapsed  = tonumber(ARGV[3])

local current  = tonumber(redis.call('GET', KEYS[1]) or '0')
local previous = tonumber(redis.call('GET', KEYS[2]) or '0')

local estimated = current + previous * ((window - elapsed) / window)

if estimated + 1 > limit then
    return {0, math.ceil(estimated)}
end

redis.call('INCR', KEYS[1])
redis.call('EXPIRE', KEYS[1], window * 2)

return {1, math.ceil(estimated + 1)}