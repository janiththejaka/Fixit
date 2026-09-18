CREATE TABLE quotation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id UUID NOT NULL,
    provider_profile_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    job_description TEXT NOT NULL,
    service_location VARCHAR(200),
    scheduled_date DATE,
    customer_note TEXT,
    quoted_price DECIMAL(12,2),
    provider_message TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    service_request_id UUID UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    CONSTRAINT fk_quotation_customer
        FOREIGN KEY (customer_profile_id)
        REFERENCES profiles(id),
    CONSTRAINT fk_quotation_provider
        FOREIGN KEY (provider_profile_id)
        REFERENCES profiles(id),
    CONSTRAINT fk_quotation_skill
        FOREIGN KEY (skill_id)
        REFERENCES skills(id),
    CONSTRAINT fk_quotation_service_request
        FOREIGN KEY (service_request_id)
        REFERENCES service_requests(id));