package io.ipfs.api.originstamp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


/**
 * Respones Object that contains the timestamp data provided by OriginStamp
 *
 * @author Thomas Hepp
 **/
public class TimestampResponseDTO implements Serializable {
    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("data")
    private Timestamp data;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public class Timestamp {
        @SerializedName("created")
        private boolean created;

        @SerializedName("date_created")
        private long dateCreated;

        @SerializedName("comment")
        private String comment;

        @SerializedName("hash_string")
        private String hashString;

        @SerializedName("timestamps")
        private List<TimestampDataDTO> timestampDataDTOList;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public boolean isCreated() {
            return created;
        }

        public void setCreated(boolean created) {
            this.created = created;
        }

        public long getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(long dateCreated) {
            this.dateCreated = dateCreated;
        }

        public String getHashString() {
            return hashString;
        }

        public void setHashString(String hashString) {
            this.hashString = hashString;
        }

        public List<TimestampDataDTO> getTimestampDataDTOList() {
            return timestampDataDTOList;
        }

        public void setTimestampDataDTOList(List<TimestampDataDTO> timestampDataDTOList) {
            this.timestampDataDTOList = timestampDataDTOList;
        }

        public class TimestampDataDTO implements Serializable {
            @SerializedName("currency_id")
            private int currency;

            @SerializedName("transaction")
            private String transaction;

            @SerializedName("private_key")
            private String privateKey;

            @SerializedName("timestamp")
            private Long timestamp;

            @SerializedName("submit_status")
            private long status;

            /**
             * 0: Bitcoin
             * 1: Ethereum
             * more to be added
             *
             * @return
             */
            public int getCurrency() {
                return currency;
            }

            public void setCurrency(int currency) {
                this.currency = currency;
            }

            public String getTransaction() {
                return transaction;
            }

            public void setTransaction(String transaction) {
                this.transaction = transaction;
            }

            public String getPrivateKey() {
                return privateKey;
            }

            public void setPrivateKey(String privateKey) {
                this.privateKey = privateKey;
            }

            public Long getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(Long timestamp) {
                this.timestamp = timestamp;
            }

            public long getStatus() {
                return status;
            }

            public void setStatus(long status) {
                this.status = status;
            }
        }
    }
}