package com.senither.hypixel.hypixel.response;

import com.senither.hypixel.inventory.ItemRarity;

import java.util.List;

public class AuctionHouseResponse {

    private boolean success;
    protected List<Auction> auctions;

    public boolean isSuccess() {
        return success;
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public class Auction {

        private String uuid;
        private String auctioneer;
        private String profile_id;
        private String item_name;
        private String item_lore;
        private String tier;
        private List<Bid> bids;
        private long starting_bid;
        private long highest_bid_amount;
        private long start;
        private long end;
        private boolean claimed;

        public String getUuid() {
            return uuid;
        }

        public String getAuctioneer() {
            return auctioneer;
        }

        public String getProfileId() {
            return profile_id;
        }

        public String getItemName() {
            return item_name;
        }

        public String getItemLore() {
            return item_lore;
        }

        public ItemRarity getTier() {
            return ItemRarity.fromName(tier);
        }

        public List<Bid> getBids() {
            return bids;
        }

        public long getStartingBid() {
            return starting_bid;
        }

        public long getHighestBidAmount() {
            return highest_bid_amount;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public boolean isClaimed() {
            return claimed;
        }

        public class Bid {

            private String auction_id;
            private String bidder;
            private long amount;
            private long timestamp;

            public String getAuctionId() {
                return auction_id;
            }

            public String getBidder() {
                return bidder;
            }

            public long getAmount() {
                return amount;
            }

            public long getTimestamp() {
                return timestamp;
            }
        }
    }
}
